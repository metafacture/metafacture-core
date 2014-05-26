/** Copyright 2013,2014 hbz
 * Licensed under the Eclipse Public License 1.0 
 **/

package org.lobid.lodmill;

import org.culturegraph.mf.framework.DefaultXmlPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A MAB XML reader.
 * 
 * @author Pascal Christoph (dr0i)
 * 
 */
@Description("A MAB XML reader")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
public final class MabXmlHandler extends DefaultXmlPipe<StreamReceiver> {

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
	public void startElement(final String uri, final String localName,
			final String qName, final Attributes attributes) throws SAXException {
		if (CONTROLLFIELD.equals(localName)) {
			builder = new StringBuilder();
			currentTag = "";
			getReceiver().startEntity(attributes.getValue(DATAFIELD_ATTRIBUTE));
		} else if (SUBFIELD.equals(localName)) {
			builder = new StringBuilder();
			currentTag = attributes.getValue(SUBFIELD_ATTRIBUTE);
		} else if (DATAFIELD.equals(localName)) {
			getReceiver().startEntity(
					attributes.getValue(DATAFIELD_ATTRIBUTE)
							+ attributes.getValue(INDICATOR1)
							+ attributes.getValue(INDICATOR2));
		} else if (RECORD.equals(localName)) {
			getReceiver().startRecord("");
		} else if (LEADER.equals(localName)) {
			builder = new StringBuilder();
			currentTag = LEADER;
		}
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String qName) throws SAXException {
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
	public void characters(final char[] chars, final int start, final int length)
			throws SAXException {
		builder.append(chars, start, length);
	}

}
