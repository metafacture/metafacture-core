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

package org.metafacture.metamorph.test.reader;

import org.metafacture.framework.StreamReceiver;

/**
 * Dynamically instantiated a reader for a specific data format. This module
 * allows to select a concrete reader only at runtime.
 *
 * @author Markus Michael Geipel
 */
public final class MultiFormatReader implements Reader {

    private static final ReaderFactory READER_FACTORY = new ReaderFactory();

    private StreamReceiver downstreamReceiver;
    private Reader currentReader;

    /**
     * Creates an instance of {@link MultiFormatReader} by a given format.
     *
     * @param format the format
     */
    public MultiFormatReader(final String format) {
        setFormat(format);
    }

    /**
     * Sets the format of {@link #currentReader} if the {@link #READER_FACTORY}
     * contains that format.
     *
     * @param format the format
     */
    public void setFormat(final String format) {
        if (!READER_FACTORY.containsKey(format)) {
            throw new IllegalArgumentException("Format '" + format + "' not regognized");
        }
        currentReader = READER_FACTORY.newInstance(format);
        currentReader.setReceiver(downstreamReceiver);
    }

    @Override
    public <R extends StreamReceiver> R setReceiver(final R streamReceiver) {
        downstreamReceiver = streamReceiver;
        return currentReader.setReceiver(streamReceiver);
    }

    @Override
    public void process(final java.io.Reader reader) {
        currentReader.process(reader);
    }

    @Override
    public void resetStream() {
        currentReader.resetStream();
    }

    @Override
    public void closeStream() {
        currentReader.closeStream();
    }

}
