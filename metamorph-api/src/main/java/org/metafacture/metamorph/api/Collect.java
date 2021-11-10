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

package org.metafacture.metamorph.api;

/**
 * Base interface for all classes which act as collectors in Metamorph.
 *
 * @author Markus Michael Geipel
 *
 */
public interface Collect extends FlushListener, ConditionAware, NamedValuePipe {
    /**
     * Flags whether to wait for a flush.
     *
     * @param waitForFlush true if to wait for a flush
     */
    void setWaitForFlush(boolean waitForFlush);

    /**
     * Flags whether the collector acts on the same entity.
     *
     * @param sameEntity true if the collector should acts on the same entity
     */
    void setSameEntity(boolean sameEntity);

    /**
     * Flags whether a reset should be done.
     *
     * @param reset true if a reset should be done.
     */
    void setReset(boolean reset);

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Sets the name.
     *
     * @param name the name
     */
    void setName(String name);

}
