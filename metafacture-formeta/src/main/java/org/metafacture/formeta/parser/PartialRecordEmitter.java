/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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

package org.metafacture.formeta.parser;

import org.metafacture.framework.StreamReceiver;

/**
 * Emits partial records.
 */
public final class PartialRecordEmitter implements Emitter {

    private StreamReceiver receiver;
    private String defaultName;

    /**
     * Creates an instance of {@link PartialRecordEmitter}.
     */
    public PartialRecordEmitter() {
    }

    /**
     * Sets the default name.
     *
     * @param defaultName the default name
     */
    public void setDefaultName(final String defaultName) {
        this.defaultName = defaultName;
    }

    /**
     * Gets the default name.
     *
     * @return the default name
     */
    public String getDefaultName() {
        return defaultName;
    }

    @Override
    public void setReceiver(final StreamReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void startGroup(final String name, final int nestingLevel) {
        if (nestingLevel == 0 && defaultName != null && name.isEmpty()) {
            receiver.startEntity(defaultName);
        }
        else {
            receiver.startEntity(name);
        }
    }

    @Override
    public void endGroup(final int nestingLevel) {
        receiver.endEntity();
    }

    @Override
    public void literal(final String name, final String value, final int nestingLevel) {
        if (nestingLevel == 0 && defaultName != null && name.isEmpty()) {
            receiver.literal(defaultName, value);
        }
        else {
            receiver.literal(name, value);
        }
    }

}
