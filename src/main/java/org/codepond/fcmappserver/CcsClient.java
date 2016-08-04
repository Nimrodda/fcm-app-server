/*
 * Modifications Copyright 2016 Nimrod Dayan
 *
 * Copyright 2014 Wolfram Rittmeyer.
 *
 * Portions Copyright Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codepond.fcmappserver;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import org.codepond.fcmappserver.messages.DownstreamMessage;
import org.codepond.fcmappserver.messages.FcmMessage;
import org.codepond.fcmappserver.messages.UpstreamMessage;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;

import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sample Smack implementation of a client for GCM Cloud Connection Server. 
 * Most of it has been taken more or less verbatim from Googles 
 * documentation: http://developer.android.com/google/gcm/ccs.html
 * <br>
 * But some additions have been made. Bigger changes are annotated like that:
 * "/// new".
 * <br>
 * Those changes have to do with parsing certain type of messages
 * as well as with sending messages to a list of recipients. The original code
 * only covers sending one message to exactly one recipient.
 */
public class CcsClient {

    public static final Logger logger = Logger.getLogger(CcsClient.class.getName());

    public static final String GCM_SERVER = "fcm-xmpp.googleapis.com";

    public static final int GCM_PORT = 5236;

    public static final String GCM_ELEMENT_NAME = "gcm";
    public static final String GCM_NAMESPACE = "google:mobile:data";

    XMPPConnection connection;
    ConnectionConfiguration config;

    /// new: some additional instance and class members
    private static CcsClient sInstance = null;
    private String mServerKey = null;
    private String mSenderId = null;
    private boolean mDebuggable = true;

    private JsonAdapter<UpstreamMessage.Request> mUpstreamRequestAdapter;
    private JsonAdapter<UpstreamMessage.Response> mUpstreamResponseAdapter;
    private JsonAdapter<DownstreamMessage.Request> mDownstreamRequestAdapter;
    private JsonAdapter<DownstreamMessage.Response> mDownstreamResponseAdapter;
    private JsonAdapter<FcmMessage> mFcmMessageAdapter;

    /**
     * XMPP Packet Extension for GCM Cloud Connection Server.
     */
    class GcmPacketExtension extends DefaultPacketExtension {

        String json;

        public GcmPacketExtension(String json) {
            super(GCM_ELEMENT_NAME, GCM_NAMESPACE);
            this.json = json;
        }

        public String getJson() {
            return json;
        }

        @Override
        public String toXML() {
            return String.format("<%s xmlns=\"%s\">%s</%s>", GCM_ELEMENT_NAME,
                    GCM_NAMESPACE, json, GCM_ELEMENT_NAME);
        }

        @SuppressWarnings("unused")
        public Packet toPacket() {
            return new Message() {
                // Must override toXML() because it includes a <body>
                @Override
                public String toXML() {

                    StringBuilder buf = new StringBuilder();
                    buf.append("<message");
                    if (getXmlns() != null) {
                        buf.append(" xmlns=\"").append(getXmlns()).append("\"");
                    }
                    if (getLanguage() != null) {
                        buf.append(" xml:lang=\"").append(getLanguage()).append("\"");
                    }
                    if (getPacketID() != null) {
                        buf.append(" id=\"").append(getPacketID()).append("\"");
                    }
                    if (getTo() != null) {
                        buf.append(" to=\"").append(StringUtils.escapeForXML(getTo())).append("\"");
                    }
                    if (getFrom() != null) {
                        buf.append(" from=\"").append(StringUtils.escapeForXML(getFrom())).append("\"");
                    }
                    buf.append(">");
                    buf.append(GcmPacketExtension.this.toXML());
                    buf.append("</message>");
                    return buf.toString();
                }
            };
        }
    }

    public static CcsClient getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("You have to prepare the client first");
        }
        return sInstance;
    }
    
    public static CcsClient prepareClient(String senderId, String serverKey, boolean debuggable) {
        synchronized(CcsClient.class) {
            if (sInstance == null) {
                sInstance = new CcsClient(senderId, serverKey, debuggable);
            }
        }
        return sInstance;
    }
    
    private CcsClient(String senderId, String serverKey, boolean debuggable) {
        this();
        mServerKey = serverKey;
        mSenderId = senderId;
        mDebuggable = debuggable;
        Moshi moshi = new Moshi.Builder().build();
        mDownstreamRequestAdapter = moshi.adapter(DownstreamMessage.Request.class);
        mDownstreamResponseAdapter = moshi.adapter(DownstreamMessage.Response.class);
        mUpstreamRequestAdapter = moshi.adapter(UpstreamMessage.Request.class);
        mUpstreamResponseAdapter = moshi.adapter(UpstreamMessage.Response.class);
        mFcmMessageAdapter = moshi.adapter(FcmMessage.class);
    }

    private CcsClient() {
        // Add GcmPacketExtension
        ProviderManager.getInstance().addExtensionProvider(GCM_ELEMENT_NAME,
                GCM_NAMESPACE, (PacketExtensionProvider) parser -> {
                    String json = parser.nextText();
                    GcmPacketExtension packet = new GcmPacketExtension(json);
                    return packet;
                });
    }

    /**
     * Sends a downstream GCM message.
     */
    public void send(String jsonRequest) {
        Packet request = new GcmPacketExtension(jsonRequest).toPacket();
        connection.sendPacket(request);
    }

    /// new: customized version of the standard handleIncomingDateMessage method
    /**
     * Handles an upstream data message from a device application.
     */
    public void handleIncomingDataMessage(UpstreamMessage.Request msg) {
        PayloadProcessor processor = ProcessorFactory.getProcessor(msg.getData().get("action"));
        processor.handleMessage(msg);
    }
    
    /**
     * Connects to GCM Cloud Connection Server using the supplied credentials.
     * @throws XMPPException
     */
    public void connect() throws XMPPException {
        config = new ConnectionConfiguration(GCM_SERVER, GCM_PORT);
        config.setSecurityMode(SecurityMode.enabled);
        config.setReconnectionAllowed(true);
        config.setRosterLoadedAtLogin(false);
        config.setSendPresence(false);
        config.setSocketFactory(SSLSocketFactory.getDefault());

        // NOTE: Set to true to launch a window with information about packets sent and received
        config.setDebuggerEnabled(mDebuggable);

        // -Dsmack.debugEnabled=true
        XMPPConnection.DEBUG_ENABLED = true;

        connection = new XMPPConnection(config);
        connection.connect();

        connection.addConnectionListener(new ConnectionListener() {

            @Override
            public void reconnectionSuccessful() {
                logger.info("Reconnecting..");
            }

            @Override
            public void reconnectionFailed(Exception e) {
                logger.log(Level.INFO, "Reconnection failed.. ", e);
            }

            @Override
            public void reconnectingIn(int seconds) {
                logger.log(Level.INFO, "Reconnecting in %d secs", seconds);
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                logger.log(Level.INFO, "Connection closed on error.");
            }

            @Override
            public void connectionClosed() {
                logger.info("Connection closed.");
            }
        });

        // Handle incoming packets
        connection.addPacketListener(packet -> {
            logger.log(Level.INFO, "Received: " + packet.toXML());
            Message incomingMessage = (Message) packet;
            GcmPacketExtension gcmPacket
                    = (GcmPacketExtension) incomingMessage.getExtension(GCM_NAMESPACE);
            String json = gcmPacket.getJson();
            handleMessage(json);
        }, new PacketTypeFilter(Message.class));

        // Log all outgoing packets
        connection.addPacketInterceptor(packet -> logger.log(Level.INFO, "Sent: {0}", packet.toXML()), new PacketTypeFilter(Message.class));

        connection.login(mSenderId + "@gcm.googleapis.com", mServerKey);
        logger.log(Level.INFO, "logged in: " + mSenderId);
    }

    private void handleMessage(String messageJson) {
        try {
            FcmMessage fcmMessage = mFcmMessageAdapter.fromJson(messageJson);
            if ("control".equals(fcmMessage.getMessageType())) {
                logger.log(Level.INFO, "Received control message");
            }
            else if ("ack".equals(fcmMessage.getMessageType())) {
                logger.log(Level.INFO, "Received ack message for " + fcmMessage.getMessageId());
            }
            else if ("nack".equals(fcmMessage.getMessageType())) {
                logger.log(Level.INFO, "Received nack message for " + fcmMessage.getMessageId());
            }
            else {
                logger.log(Level.INFO, "Received upstream message");
                UpstreamMessage.Request upStreamMessage = mUpstreamRequestAdapter.fromJson(messageJson);
                handleIncomingDataMessage(upStreamMessage);
                // Send mandatory ACK to CCS
                String json = mUpstreamResponseAdapter.toJson(new UpstreamMessage.Response(upStreamMessage.getFrom(), upStreamMessage.getMessageId()));
                send(json);
            }
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Error parsing JSON " + messageJson, e);
        }

    }

    public static void main(String[] args) throws XMPPException, IOException {
        final String propertiesFile;
        if (args.length > 0 && args[0] != null) {
            propertiesFile = args[0];
        }
        else {
            propertiesFile = "fcm-app-server.properties";
        }

        File config = new File(propertiesFile);
        try (FileInputStream fileInputStream = new FileInputStream(config)) {
            logger.log(Level.INFO, "Loading configuration from file: " + propertiesFile);
            Properties properties = new Properties();
            properties.load(fileInputStream);

            String senderId = properties.getProperty("org.codepond.fcmappserver.senderId");
            String serverKey = properties.getProperty("org.codepond.fcmappserver.serverKey");
            if (!senderId.isEmpty() && !serverKey.isEmpty()) {
                CcsClient ccsClient = CcsClient.prepareClient(senderId, serverKey, true);
                ccsClient.connect();
            }
            else {
                System.out.println("Sender ID/Server Key is not configured. Terminating...");
            }
        }
    }
}
