/** Copyright 2013,214 hbz, Pascal Christoph.
 * Licensed under the Eclipse Public License 1.0
 **/

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
	public void characters(final char[] chars, final int start, final int length)
			throws SAXException {
		this.builder.append(chars, start, length);
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {
		if (MabXmlHandler.CONTROLLFIELD.equals(localName)) {
			getReceiver().literal(this.currentTag, this.builder.toString().trim());
			getReceiver().endEntity();
		} else if (MabXmlHandler.SUBFIELD.equals(localName)) {
			getReceiver().literal(this.currentTag, this.builder.toString().trim());
		} else if (MabXmlHandler.DATAFIELD.equals(localName)) {
			getReceiver().endEntity();
		} else if (MabXmlHandler.RECORD.equals(localName)) {
			getReceiver().endRecord();
		}
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName,
			final Attributes attributes) throws SAXException {
		if (MabXmlHandler.CONTROLLFIELD.equals(localName)) {
			this.builder = new StringBuilder();
			this.currentTag = "";
			getReceiver().startEntity(attributes.getValue(MabXmlHandler.DATAFIELD_ATTRIBUTE));
		} else if (MabXmlHandler.SUBFIELD.equals(localName)) {
			this.builder = new StringBuilder();
			this.currentTag = attributes.getValue(MabXmlHandler.SUBFIELD_ATTRIBUTE);
		} else if (MabXmlHandler.DATAFIELD.equals(localName)) {
			getReceiver().startEntity(
					attributes.getValue(MabXmlHandler.DATAFIELD_ATTRIBUTE)
					+ attributes.getValue(MabXmlHandler.INDICATOR1)
					+ attributes.getValue(MabXmlHandler.INDICATOR2));
		} else if (MabXmlHandler.RECORD.equals(localName)) {
			getReceiver().startRecord("");
		} else if (MabXmlHandler.LEADER.equals(localName)) {
			this.builder = new StringBuilder();
			this.currentTag = MabXmlHandler.LEADER;
		}
	}

}
