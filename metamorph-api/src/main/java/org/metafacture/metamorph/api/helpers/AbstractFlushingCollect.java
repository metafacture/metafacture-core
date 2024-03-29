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

/**
 * Common base for collectors.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
public abstract class AbstractFlushingCollect extends AbstractCollect {

    private boolean flushIncomplete = true;

    /**
     * Flags if to flush when incomplete.
     *
     * @param flushIncomplete true if it should be flushed when incomplete
     */
    public final void setFlushIncomplete(final boolean flushIncomplete) {
        this.flushIncomplete = flushIncomplete;
    }

    @Override
    public final void flush(final int recordCount, final int entityCount) {
        if (isSameRecord(recordCount) && sameEntityConstraintSatisfied(entityCount)) {
            if (isConditionMet() && (flushIncomplete || isComplete())) {
                emit();
            }
            if (getReset()) {
                resetCondition();
                clear();
            }
        }
    }

}
