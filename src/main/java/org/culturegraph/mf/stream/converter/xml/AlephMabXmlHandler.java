/** Copyright 2013,2014 hbz
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
import org.xml.sax.SAXException;

/**
 * An Aleph-MAB-XML reader.
 * 
 * @author Pascal Christoph (dr0i)
 * 
 */
@Description("A MAB XML reader")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
public final class AlephMabXmlHandler extends DefaultXmlPipe<StreamReceiver> {

	private static final String SUBFIELD = "subfield";
	private static final String DATAFIELD = "datafield";
	private static final String CONTROLLFIELD = "controlfield";
	private static final String RECORD = "ListRecords";
	private static final String LEADER = "leader";
	private static final String DATAFIELD_ATTRIBUTE = "tag";
	private static final String SUBFIELD_ATTRIBUTE = "code";
	private static final String INDICATOR1 = "ind1";
	private static final String INDICATOR2 = "ind2";
	private String currentTag = "";
	private StringBuilder builder = new StringBuilder();

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
			throws SAXException {
		if (CONTROLLFIELD.equals(localName)) {
			builder = new StringBuilder();
			currentTag = "";
			getReceiver().startEntity(attributes.getValue(DATAFIELD_ATTRIBUTE));
		} else if (SUBFIELD.equals(localName)) {
			builder = new StringBuilder();
			currentTag = attributes.getValue(SUBFIELD_ATTRIBUTE);
		} else if (DATAFIELD.equals(localName)) {
			getReceiver().startEntity(attributes.getValue(DATAFIELD_ATTRIBUTE) + attributes.getValue(INDICATOR1)
					+ attributes.getValue(INDICATOR2));
		} else if (RECORD.equals(localName)) {
			getReceiver().startRecord("");
		} else if (LEADER.equals(localName)) {
			builder = new StringBuilder();
			currentTag = LEADER;
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (CONTROLLFIELD.equals(localName)) {
			getReceiver().literal(currentTag, builder.toString().trim());
			getReceiver().endEntity();
		} else if (SUBFIELD.equals(localName)) {
			getReceiver().literal(currentTag, builder.toString().trim());
		} else if (DATAFIELD.equals(localName)) {
			getReceiver().endEntity();
		} else if (RECORD.equals(localName)) {
			getReceiver().endRecord();
		}
	}

	@Override
	public void characters(final char[] chars, final int start, final int length) throws SAXException {
		builder.append(chars, start, length);
	}

}
