/** Copyright 2013,214 hbz, Pascal Christoph.
 * Licensed under the Eclipse Public License 1.0
 **/
package org.culturegraph.mf.stream.converter.xml;

import java.util.HashSet;

import org.apache.commons.lang.StringEscapeUtils;
import org.culturegraph.mf.framework.DefaultXmlPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * An XML entity splitter.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
@Description("Splits all entities (aka records) residing in one XML document into multiple single XML documents.")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
public final class XmlEntitySplitter extends DefaultXmlPipe<StreamReceiver> {

	/**
	 * Returns the XML declaration which is hard coded. @TODO change that hard
	 * wired.
	 *
	 * @return the XML decalration
	 */
	public static String getXmlDeclaration() {
		return XmlEntitySplitter.XML_DECLARATION;
	}

	private String entity;
	private StringBuilder builder = new StringBuilder();
	private final HashSet<String> namespaces = new HashSet<String>();
	private boolean inEntity = false;
	private int recordCnt = 0;
	private String root;
	private final static String XML_DECLARATION = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>";

	private int entityDepth = 0;

	private void appendValuesToEntity(final String qName, final Attributes attributes) {
		this.builder.append("<" + qName);
		if (attributes.getLength() > 0) {
			for (int i = 0; i < attributes.getLength(); i++) {
				this.builder.append(" " + attributes.getQName(i) + "=\""
						+ StringEscapeUtils.escapeXml(attributes.getValue(i)) + "\"");
			}
		}

		this.builder.append(">");
	}

	@Override
	public void characters(final char[] chars, final int start, final int length)
			throws SAXException {
		try {
			this.builder.append(StringEscapeUtils.escapeXml(new String(chars, start, length)));
		} catch (final Exception e) {
			reset();
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {
		if (this.inEntity) {
			this.builder.append("</" + qName + ">");
			if (this.entity.equals(localName)) {
				if (this.entityDepth <= 1) {
					final StringBuilder sb = new StringBuilder(XmlEntitySplitter.XML_DECLARATION
							+ "<" + this.root);
					if (this.namespaces != null) {
						for (final String ns : this.namespaces) {
							sb.append(ns);
						}
						sb.append(">");
					}
					this.builder.insert(0, sb.toString()).append("</" + this.root + ">");
					getReceiver().literal("entity", this.builder.toString());
					getReceiver().endRecord();
					reset();
					return;
				}
				this.entityDepth--;
			}
		}
	}

	@Override
	public void onResetStream() {
		reset();
	}

	private void reset() {
		this.inEntity = false;
		this.builder = new StringBuilder();
		this.entityDepth = 0;
	}

	/**
	 * Sets the name of the entity. All these entities in the XML stream will be
	 * XML documents on their own.
	 *
	 * @param name
	 *            Identifies the entities
	 */
	public void setEntityName(final String name) {
		this.entity = name;
	}

	/**
	 * Sets the top-level XML document element.
	 *
	 * @param name
	 *            the element
	 */
	public void setTopLevelElement(final String name) {
		this.root = name;
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName,
			final Attributes attributes) throws SAXException {
		if (!this.inEntity) {
			if (this.entity.equals(localName)) {
				this.builder = new StringBuilder();
				getReceiver().startRecord(String.valueOf(this.recordCnt++));
				this.inEntity = true;
				appendValuesToEntity(qName, attributes);
				this.entityDepth++;
			} else if (this.root == null) {
				this.root = qName;
			}
		} else {
			if (this.entity.equals(localName)) {
				this.entityDepth++;
			}
			appendValuesToEntity(qName, attributes);
		}
	}

	@Override
	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
		super.startPrefixMapping(prefix, uri);
		if (!prefix.isEmpty() && uri != null) {
			this.namespaces.add(" xmlns:" + prefix + "=\"" + uri + "\"");
		}
	}
}
