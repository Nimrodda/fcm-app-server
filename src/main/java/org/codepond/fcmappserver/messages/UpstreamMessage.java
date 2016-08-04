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
 * Represents a single message sent by a device via FCM to the app server
 * https://firebase.google.com/docs/cloud-messaging/xmpp-server-ref#upstream
 */
public class UpstreamMessage {
	/**
     * Upstream message received from FCM
     */
    public static class Request extends FcmMessage {
        public Request(String message_type, String from, String catergory, String message_id, Map<String, String> data) {
            super(message_type, message_id);
        	this.from = from;
            this.catergory = catergory;
            this.data = data;
        }

        /**
         * This parameter specifies who sent the message. The value is the registration token of the client app.
         */
        private String from;
        /**
         * This parameter specifies the application package name of the client app that sent the message.
         */
        private String catergory;
        /**
         * his parameter specifies the key-value pairs of the message's payload.
         */
        private Map<String, String> data;

		public String getFrom() {
			return from;
		}

		public String getCatergory() {
			return catergory;
		}

		public Map<String, String> getData() {
			return data;
		}
	}

    /**
     * Response to an upstream message received from FCM.
     * According to the documentation, the app server must always respond with an 'ack' message type.
     */
    public static class Response extends FcmMessage {
        /**
         *
         * @param to This parameter specifies the recipient of a response message. The value must be a registration token of the client app that sent the upstream message.
         * @param message_id This parameter specifies which message the response is intended for. The value must be the message_id value from the corresponding upstream message.
         */
        public Response(String to, String message_id) {
            super("ack", message_id); // Must always be 'ack' per the documentation
            this.to = to;
        }

        /**
         * This parameter specifies the recipient of a response message.
         * The value must be a registration token of the client app that sent the upstream message.
         */
        private String to;

		public String getTo() {
			return to;
		}
	}
}
