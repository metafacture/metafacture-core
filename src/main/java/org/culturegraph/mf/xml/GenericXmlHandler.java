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
package org.culturegraph.mf.xml;

import java.util.regex.Pattern;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultXmlPipe;
import org.xml.sax.Attributes;


/**
 * A generic xml reader.
 *
 * @author Markus Michael Geipel
 *
 */
@Description("A generic xml reader")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("handle-generic-xml")
public final class GenericXmlHandler extends DefaultXmlPipe<StreamReceiver> {

	private static final Pattern TABS = Pattern.compile("\t+");
	private final String recordTagName;
	private boolean inRecord;
	private StringBuilder valueBuffer = new StringBuilder();

	public GenericXmlHandler() {
		super();
		this.recordTagName = System.getProperty(
				"org.culturegraph.metamorph.xml.recordtag");
		if (recordTagName == null) {
			throw new MetafactureException("Missing name for the tag marking a record.");
		}
	}

	public GenericXmlHandler(final String recordTagName) {
		super();
		this.recordTagName = recordTagName;
	}

	@Override
	public void startElement(final String uri, final String localName,
			final String qName, final Attributes attributes) {

		if (inRecord) {
			writeValue();
			getReceiver().startEntity(localName);
			writeAttributes(attributes);
		} else if (localName.equals(recordTagName)) {
			final String identifier = attributes.getValue("id");
			if (identifier == null) {
				getReceiver().startRecord("");
			} else {
				getReceiver().startRecord(identifier);
			}
			writeAttributes(attributes);
			inRecord = true;
		}
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String qName) {
		if (inRecord) {
			writeValue();
			if (localName.equals(recordTagName)) {
				inRecord = false;
				getReceiver().endRecord();
			} else {
				getReceiver().endEntity();
			}
		}
	}

	@Override
	public void characters(final char[] chars, final int start, final int length) {
		if (inRecord) {
			valueBuffer.append(TABS.matcher(new String(chars, start, length))
					.replaceAll(""));
		}
	}

	private void writeValue() {
		final String value = valueBuffer.toString();
		if (!value.trim().isEmpty()) {
			getReceiver().literal("value", value.replace('\n', ' '));
		}
		valueBuffer = new StringBuilder();
	}

	private void writeAttributes(final Attributes attributes) {
		final int length = attributes.getLength();

		for (int i = 0; i < length; ++i) {
			final String name = attributes.getLocalName(i);
			final String value = attributes.getValue(i);
			getReceiver().literal(name, value);
		}
	}

}
