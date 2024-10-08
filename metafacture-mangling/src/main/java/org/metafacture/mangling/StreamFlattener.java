/*
 * Copyright 2016 Deutsche Nationalbibliothek
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

package org.metafacture.mangling;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

/**
 * Flattens all entities in a stream by prefixing the literals with the entity
 * paths. The stream emitted by this module is guaranteed to not contain any
 * <i>start-entity</i> and <i>end-entity</i> events.
 * <p>
 * For example, take the following sequence of events:
 * <pre>{@literal
 * start-record "1"
 * literal "top-level": literal-value
 * start-entity "entity"
 * literal "nested": literal-value
 * end-entity
 * end-record
 * }</pre>
 *
 * These events are transformed by the {@code StreamFlattener} into the
 * following sequence of events:
 * <pre>{@literal
 * start-record "1"
 * literal "top-level": literal-value
 * literal "entity.nested": literal-value
 * end-record
 * }</pre>
 *
 * @author Christoph Böhme (rewrite)
 * @author Markus Michael Geipel
 * @see EntityPathTracker
 *
 */
@Description("flattens out entities in a stream by introducing dots in literal names")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("flatten")
public final class StreamFlattener extends DefaultStreamPipe<StreamReceiver> {

    public static final String DEFAULT_ENTITY_MARKER = ".";

    private final EntityPathTracker pathTracker = new EntityPathTracker();

    /**
     * Creates an instance of {@link StreamFlattener}.
     */
    public StreamFlattener() {
        setEntityMarker(DEFAULT_ENTITY_MARKER);
    }

    /**
     * Gets the entity marker.
     *
     * @return the entity marker
     */
    public String getEntityMarker() {
        return pathTracker.getEntitySeparator();
    }

    /**
     * Sets the entity marker.
     *
     * @param entityMarker the entity marker
     */
    public void setEntityMarker(final String entityMarker) {
        pathTracker.setEntitySeparator(entityMarker);
    }

    @Override
    public void startRecord(final String identifier) {
        assert !isClosed();
        pathTracker.startRecord(identifier);
        getReceiver().startRecord(identifier);
    }

    @Override
    public void endRecord() {
        assert !isClosed();
        pathTracker.endRecord();
        getReceiver().endRecord();
    }

    @Override
    public void startEntity(final String name) {
        assert !isClosed();
        pathTracker.startEntity(name);
    }

    @Override
    public void endEntity() {
        assert !isClosed();
        pathTracker.endEntity();
    }

    @Override
    public void literal(final String name, final String value) {
        assert !isClosed();
        getReceiver().literal(pathTracker.getCurrentPathWith(name), value);
    }

    /**
     * Gets the current entity name.
     *
     * @return the current entity name
     */
    public String getCurrentEntityName() {
        return pathTracker.getCurrentEntityName();
    }

    /**
     * Gets the current path.
     *
     * @return the current path
     */
    public String getCurrentPath() {
        return pathTracker.getCurrentPath();
    }

}
