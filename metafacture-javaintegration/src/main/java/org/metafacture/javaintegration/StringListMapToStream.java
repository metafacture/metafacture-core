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

import org.metafacture.commons.types.ListMap;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.util.List;
import java.util.Map.Entry;

/**
 * Reads a {@link ListMap} and sends it to a {@link StreamReceiver}
 *
 * @author Markus Michael Geipel, Christoph Böhme
 *
 */
@In(ListMap.class)
@Out(StreamReceiver.class)
@FluxCommand("string-list-map-to-stream")
public final class StringListMapToStream extends DefaultObjectPipe<ListMap<String, String>, StreamReceiver> {

    /**
     * Creates an instance of {@link StringListMapToStream}.
     */
    public StringListMapToStream() {
    }

    @Override
    public void process(final ListMap<String, String> listMap) {
        assert !isClosed();
        process(listMap, getReceiver());
    }

    /**
     * Reads a {@link ListMap} and sends it to a {@link StreamReceiver}.
     *
     * @param listMap  the ListMap to be read
     * @param receiver the StreamReceiver to send to
     */
    public static void process(final ListMap<String, String> listMap, final StreamReceiver receiver) {
        receiver.startRecord(listMap.getId());
        for (final Entry<String, List<String>> entry: listMap.entrySet()) {
            final String name = entry.getKey();
            for (final String value:entry.getValue()) {
                receiver.literal(name, value);
            }
        }
        receiver.endRecord();
    }

}
