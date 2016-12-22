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

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.XmlPipe;
import org.culturegraph.mf.xml.XmlDecoder;


/**
 * Base class for {@link Reader}s for xml formats.
 *
 * @author Christoph Böhme
 *
 */
class XmlReaderBase implements Reader {

	private final XmlDecoder xmlDecoder = new XmlDecoder();
	private final XmlPipe<StreamReceiver> xmlHandler;

	XmlReaderBase(final XmlPipe<StreamReceiver> xmlHandler) {
		this.xmlHandler = xmlHandler;
		xmlDecoder.setReceiver(this.xmlHandler);
	}

	@Override
	public final <R extends StreamReceiver> R setReceiver(final R receiver) {
		xmlHandler.setReceiver(receiver);
		return receiver;
	}

	@Override
	public final void process(final java.io.Reader reader) {
		xmlDecoder.process(reader);
	}

	@Override
	public final void resetStream() {
		xmlDecoder.resetStream();
	}

	@Override
	public final void closeStream() {
		xmlDecoder.closeStream();
	}

}
