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

/**
 * Base class for downstream and upstream messages
 */
public class FcmMessage {
    /**
     * This parameter specifies the type of the CCS message: either delivery receipt or control.
     * When it is set to receipt, the message includes from, message_id, category, and data fields to provide additional information.
     * When it is set to control, the message includes control_type to indicate the type of control message.
     */
	private String message_type;
    /**
     * This parameter uniquely identifies a message in an XMPP connection. The value is a string that uniquely identifies the associated message.
     *
     */
	private String message_id;

	public FcmMessage(String message_type, String message_id) {
		this.message_type = message_type;
        this.message_id = message_id;
    }

	public String getMessageType() {
		return message_type;
	}

    public String getMessageId() {
        return message_id;
    }
}
