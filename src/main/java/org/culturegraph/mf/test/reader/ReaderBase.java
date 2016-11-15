/*
 * Copyright 2013, 2014, 2016 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.test.reader;

import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;


/**
 * Base class for {@link Reader}s that are constructed from a record reader and
 * a record decoder.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
class ReaderBase implements Reader {

	private final ObjectPipe<java.io.Reader, ObjectReceiver<String>> recordReader;
	private final ObjectPipe<String, StreamReceiver> decoder;

	ReaderBase(
			final ObjectPipe<java.io.Reader, ObjectReceiver<String>> recordReader,
			final ObjectPipe<String, StreamReceiver> decoder) {
		this.recordReader = recordReader;
		this.decoder = decoder;
		this.recordReader.setReceiver(this.decoder);
	}

	@Override
	public final <R extends StreamReceiver> R setReceiver(final R receiver) {
		decoder.setReceiver(receiver);
		return receiver;
	}

	@Override
	public final void process(final java.io.Reader reader) {
		recordReader.process(reader);
	}

	@Override
	public final void resetStream() {
		recordReader.resetStream();
	}

	@Override
	public final void closeStream() {
		recordReader.closeStream();
	}

}
