/*
 * Copyright 2016 Nimrod Dayan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codepond.fcmappserver.messages;

import java.util.Map;

/**
 * Represents a single downstream message
 * https://firebase.google.com/docs/cloud-messaging/xmpp-server-ref#send-downstream
 */
public class DownstreamMessage {
    /**
     * Downstream message sent by the app server to FCM
     */
    public static class Request extends FcmMessage {
        public Request(String to, String message_id, Map<String, String> data) {
            super(null, message_id);
            this.to = to;
            this.data = data;
        }

        /**
         * This parameter specifies the recipient of a message.
         * The value must be a registration token, notification key, or topic.
         * Do not set this field when sending to multiple topics. See condition.
         */
        private String to;
        /**
         * This parameter specifies the key-value pairs of the message's payload.
         * For example, with data:{"score":"3x1"}
         */
        private Map<String, String> data;

        public String getTo() {
            return to;
        }

        public Map<String, String> getData() {
            return data;
        }
    }

    /**
     * Receipt sent by FCM to the app server a {@link Request}
     */
    public static class Response extends FcmMessage {
        public Response(String from, String message_id, String message_type, String registration_id, String error, String error_description) {
            super(message_type, message_id);
        	this.from = from;
            this.registration_id = registration_id;
            this.error = error;
            this.error_description = error_description;
        }

        /**
         * This parameter specifies who sent this response.
         * The value is the registration token of the client app.
         */
        private String from;
        /**
         * This parameter specifies the canonical registration token for the client app that the message was processed and sent to.
         * Sender should replace the registration token with this value on future requests; otherwise, the messages might be rejected.
         */
        private String registration_id;
        /**
         * This parameter specifies an error related to the downstream message. It is set when the message_type is nack. See table 4 for details.
         */
        private String error;
        /**
         * This parameter provides descriptive information for the error. It is set when the message_type is nack.
         */
        private String error_description;
    }
}
