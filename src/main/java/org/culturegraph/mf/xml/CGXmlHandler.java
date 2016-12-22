/*
 * Copyright 2016 Christoph Böhme
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

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.FormatException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.XmlReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultXmlPipe;
import org.xml.sax.Attributes;

/**
 * Decodes an CG-XML document into an event stream.
 * <p>
 * The GG-XML format is a simple XML schema for writing event streams into XML
 * documents. The following example shows a complete CG-XML document:
 * <pre>{@literal
 * <?xml version="1.0" encoding="UTF-8"?>
 * <cgxml xmlns="http://www.culturegraph.org/cgxml" version="1.0">
 *   <records>
 *     <record id="1">
 *       <literal name="Name" value="Thomas Mann" />
 *       <entity name="Address">
 *         <entity name="Street">
 *           <literal name="Street" value="Alte Landstrasse" />
 *           <literal name="Number" value="39" />
 *         </entity>
 *         <literal name="City" value="Kilchberg" />
 *         <literal name="Postcode" />  <!-- the value attribute is optional -->
 *       </entity>
 *     </record>
 *     <record> <!-- the id attribute is optional -->
 *     </record>
 *   </records>
 * </cgxml>
 * }</pre>
 *
 * @author Christoph Böhme
 *
 */
@Description("Reads CG-XML files")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("handle-cg-xml")
public final class CGXmlHandler extends DefaultXmlPipe<StreamReceiver> {

	public static final String CGXML_NAMESPACE =
			"http://www.culturegraph.org/cgxml";

	private static final String ROOT_TAG = "cgxml";
	private static final String RECORDS_TAG = "records";
	private static final String RECORD_TAG = "record";
	private static final String ENTITY_TAG = "entity";
	private static final String LITERAL_TAG = "literal";

	private static final String VERSION_ATTR = "version";
	private static final String ID_ATTR = "id";
	private static final String NAME_ATTR = "name";
	private static final String VALUE_ATTR = "value";

	private static final String VERSION = "1.0";

	@Override
	public void startElement(final String uri, final String localName,
			final String qName, final Attributes attributes) {
		if (!CGXML_NAMESPACE.equals(uri)) {
			return;
		}
		switch (localName) {
			case ROOT_TAG:
				verifyValidVersion(attributes);
				break;
			case RECORDS_TAG:
				// Nothing to do
				break;
			case RECORD_TAG:
				emitStartRecord(attributes);
				break;
			case ENTITY_TAG:
				emitStartEntity(attributes);
				break;
			case LITERAL_TAG:
				emitLiteral(attributes);
				break;
			default:
				throw new FormatException("Unexpected element: " + localName);
		}
	}

	private void verifyValidVersion(final Attributes attributes) {
		final String version = attributes.getValue("", VERSION_ATTR);
		if (!VERSION.equals(version)) {
			throw new FormatException("Invalid cg-xml version: " + version);
		}
	}

	private void emitStartRecord(final Attributes attributes) {
		final String recordId = attributes.getValue("", ID_ATTR);
		if (recordId == null) {
			getReceiver().startRecord("");
		} else {
			getReceiver().startRecord(recordId);
		}
	}

	private void emitStartEntity(final Attributes attributes) {
		final String name = attributes.getValue("", NAME_ATTR);
		if (name == null) {
			throw new FormatException("Entity name must not be null");
		}
		getReceiver().startEntity(name);
	}

	private void emitLiteral(final Attributes attributes) {
		final String name = attributes.getValue("", NAME_ATTR);
		final String value = attributes.getValue("", VALUE_ATTR);
		if (name == null) {
			throw new FormatException("Literal name must not be null");
		}
		getReceiver().literal(name, value);
	}

	@Override
	public void endElement(final String uri, final String localName,
			final String qName) {
		if (!CGXML_NAMESPACE.equals(uri)) {
			return;
		}
		if (RECORD_TAG.equals(localName)) {
			getReceiver().endRecord();
		} else if (ENTITY_TAG.equals(localName)) {
			getReceiver().endEntity();
		}
	}

}
