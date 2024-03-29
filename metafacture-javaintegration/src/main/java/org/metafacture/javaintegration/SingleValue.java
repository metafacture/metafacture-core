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

package org.metafacture.javaintegration;

import org.metafacture.framework.helpers.DefaultStreamReceiver;

import java.util.Collection;

/**
 * Just records the value of the last received literal.
 * @author Markus Michael Geipel
 *
 */
public final class SingleValue extends DefaultStreamReceiver implements Collector<String> {

    private boolean closed;
    private String value = "";
    private Collection<String> collection;

    /**
     * Creates an instance of {@link SingleValue}.
     */
    public SingleValue() {
    }

    /**
     * Creates an instance of {@link SingleValue} by a given Collection.
     *
     * @param collection the Collection
     */
    public SingleValue(final Collection<String> collection) {
        this.collection = collection;
    }

    /**
     * Check whether SingleValue is closed.
     *
     * @return true if SingleValue is closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @return collected value, if nothing collected return ""
     */
    public String getValue() {
        return value;
    }

    private void setValue(final String value) {
        this.value = value;
    }

    @Override
    public void startRecord(final String identifier) {
        this.setValue("");

    }

    @Override
    public void endRecord() {
        assert !closed;
        if (collection != null) {
            collection.add(value);
        }
    }

    @Override
    public void literal(final String name, final String newValue) {
        assert !closed;
        setValue(newValue);
    }

    @Override
    public void resetStream() {
        value = "";
        closed = false;
    }

    @Override
    public void closeStream() {
        closed = true;
    }

    @Override
    public Collection<String> getCollection() {
        return collection;
    }

    @Override
    public void setCollection(final Collection<String> collection) {
        this.collection = collection;
    }

}
