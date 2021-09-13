/*
 * Copyright 2016 Christoph Böhme
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

package org.metafacture.metamorph.api.helpers;

import org.metafacture.metamorph.api.NamedValueSource;

/**
 * Base class for functions which maintain a state between invocations.
 *
 * @author Markus Michael Geipel
 */
public abstract class AbstractStatefulFunction extends AbstractFunction {

    private int recordCount;
    private int entityCount;
    private NamedValueSource source;
    private String lastName;

    protected final int getRecordCount() {
        return recordCount;
    }

    protected final int getEntityCount() {
        return entityCount;
    }

    protected final NamedValueSource getNamedValueSource() {
        return source;
    }

    protected final String getLastName() {
        return lastName;
    }

    @Override
    public final void receive(final String currentName, final String value,
            final NamedValueSource currentSource, final int currentRecordCount,
            final int currentEntityCount) {

        if (!sameRecord(currentRecordCount)) {
            reset();
            recordCount = currentRecordCount;
        }
        if (entityClearNeeded(currentEntityCount)) {
            reset();
        }
        entityCount = currentEntityCount;
        source = currentSource;
        lastName = currentName;

        final String processedValue = process(value);
        if (processedValue == null) {
            return;
        }

        getNamedValueReceiver().receive(currentName, processedValue, this, currentRecordCount, currentEntityCount);
    }

    private boolean entityClearNeeded(final int currentEntityCount) {
        return doResetOnEntityChange() && entityCount != currentEntityCount;
    }

    private boolean sameRecord(final int currentRecordCount) {
        return recordCount == currentRecordCount;
    }

    protected abstract String process(String value);

    protected abstract void reset();

    protected abstract boolean doResetOnEntityChange();

}
