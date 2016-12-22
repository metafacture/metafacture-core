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
package org.culturegraph.mf.jdom;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.XmlPipe;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.SAXOutputter;

/**
 * Converts a {@link Document} to a stream.
 *
 * @author Markus Geipel
 */
@In(Document.class)
@Out(StreamReceiver.class)
@FluxCommand("from-jdom-document")
public final class JDomDocumentToStream
		implements ObjectPipe<Document, StreamReceiver> {

	private final SAXOutputter saxOutputer;
	private final XmlPipe<StreamReceiver> xmlPipe;

	public JDomDocumentToStream(final XmlPipe<StreamReceiver> xmlPipe) {
		this.xmlPipe = xmlPipe;
		saxOutputer = new SAXOutputter(xmlPipe);
	}

	@Override
	public void process(final Document document) {
		assert null!=document;
		try {
			saxOutputer.output(document);
		} catch (JDOMException e) {
			throw new IllegalArgumentException("Invalid JDOM document", e);
		}
	}

	@Override
	public void resetStream() {
		xmlPipe.resetStream();
	}

	@Override
	public void closeStream() {
		xmlPipe.closeStream();

	}

	@Override
	public <R extends StreamReceiver> R setReceiver(final R receiver) {
		return xmlPipe.setReceiver(receiver);
	}

}
