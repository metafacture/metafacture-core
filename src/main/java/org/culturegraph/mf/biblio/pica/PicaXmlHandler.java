/*
 * Copyright 2014 hbz, Pascal Christoph
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
package org.culturegraph.mf.biblio.pica;

import java.text.Normalizer;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultXmlPipe;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A pica xml handler.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
@Description("A pica xml reader")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("handle-picaxml")
public final class PicaXmlHandler extends DefaultXmlPipe<StreamReceiver> {

	private static final String SUBFIELD = "subf";
	private static final String DATAFIELD = "tag";
	private static final String RECORD = "record";
	private static final String NAMESPACE =
			"http://www.oclcpica.org/xmlns/ppxml-1.0";
	private static final String LEADER = "global";
	private String currentTag = "";
	private StringBuilder builder = new StringBuilder();

	@Override
	public void startElement(final String uri, final String localName,
			final String qName, final Attributes attributes) throws SAXException {
		if (SUBFIELD.equals(localName)) {
			builder = new StringBuilder();
			currentTag = attributes.getValue("id");
		} else if (DATAFIELD.equals(localName)) {
			getReceiver().startEntity(
					attributes.getValue("id") + attributes.getValue("occ"));
		} else if (RECORD.equals(localName) && NAMESPACE.equals(uri)) {
			getReceiver().startRecord("");
		} else if (LEADER.equals(localName)) {
			builder = new StringBuilder();
			currentTag = LEADER;
		}
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String qName) throws SAXException {
		if (SUBFIELD.equals(localName)) {
			getReceiver().literal(currentTag,
					Normalizer.normalize(builder.toString().trim(), Normalizer.Form.NFC));
		} else if (DATAFIELD.equals(localName)) {
			getReceiver().endEntity();
		} else if (RECORD.equals(localName) && NAMESPACE.equals(uri)) {
			getReceiver().endRecord();
		}
	}

	@Override
	public void characters(final char[] chars, final int start, final int length)
			throws SAXException {
		builder.append(chars, start, length);
	}

}
