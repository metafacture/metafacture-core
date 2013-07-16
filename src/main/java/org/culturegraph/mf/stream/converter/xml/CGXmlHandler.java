/*
 *  Copyright 2013 Deutsche Nationalbibliothek
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.culturegraph.mf.stream.converter.xml;

import org.culturegraph.mf.framework.DefaultXmlPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.xml.sax.Attributes;


/**
 * Reads CG-XML files.
 *
 * @author Christoph Böhme
 *
 */
@Description("Reads CG-XML files")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
public final class CGXmlHandler extends DefaultXmlPipe<StreamReceiver> {

	private static final String RECORD_TAG = "record";
	private static final String ENTITY_TAG = "entity";
	private static final String LITERAL_TAG = "literal";
	private static final String ID_ATTR = "id";
	private static final String NAME_ATTR = "name";
	private static final String VALUE_ATTR = "value";

	@Override
	public void startElement(final String uri, final String localName,
			final String qName, final Attributes attributes) {
		if (RECORD_TAG.equals(localName)) {
			final String recordId = attributes.getValue("", ID_ATTR);
			if (recordId == null) {
				getReceiver().startRecord("");
			} else {
				getReceiver().startRecord(recordId);
			}
		} else if (ENTITY_TAG.equals(localName)) {
			getReceiver().startEntity(attributes.getValue("", NAME_ATTR));
		} else if (LITERAL_TAG.equals(localName)) {
			getReceiver().literal(attributes.getValue("", NAME_ATTR),
					attributes.getValue("", VALUE_ATTR));
		}
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String qName) {
		if (RECORD_TAG.equals(localName)) {
			getReceiver().endRecord();
		} else if (ENTITY_TAG.equals(localName)) {
			getReceiver().endEntity();
		}
	}

}
