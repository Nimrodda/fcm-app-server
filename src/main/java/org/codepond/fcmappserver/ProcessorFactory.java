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

public class ProcessorFactory {

    private static final String PACKAGE = "org.codepond.fcmappserver";
    private static final String ACTION_REGISTER = PACKAGE + ".REGISTER";
    private static final String ACTION_MESSAGE = PACKAGE + ".MESSAGE";

    public static PayloadProcessor getProcessor(String action) {
        if (ACTION_REGISTER.equals(action)) {
            return new RegisterProcessor();
        }
        else if (ACTION_MESSAGE.equals(action)) {
            return new MessageProcessor();
        }
        throw new IllegalStateException("Action " + action + " is unknown");
    }
}
