/*
 * Copyright 2018 Deutsche Nationalbibliothek
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
package org.metafacture.solr;

import org.metafacture.framework.ObjectReceiver;

/**
 * Stores the last received object. A buffer of size one.
 */
public class ObjectBuffer<T> implements ObjectReceiver<T> {

    private T obj;

    public ObjectBuffer() {
    }

    @Override
    public void resetStream() {
        this.obj = null;
    }

    @Override
    public void closeStream() {

    }

    @Override
    public void process(T obj) {
        this.obj = obj;
    }

    T getObject() {
        return obj;
    }
}
