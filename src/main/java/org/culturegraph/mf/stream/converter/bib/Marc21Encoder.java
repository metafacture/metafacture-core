/*
 *  Copyright 2014 Christoph Böhme
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
package org.culturegraph.mf.stream.converter.bib;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.iso2709.Iso2709Format;
import org.culturegraph.mf.iso2709.RecordBuilder;
import org.culturegraph.mf.iso2709.RecordFormat;

/**
 * Encodes a stream in MARC21 format.
 *
 * <p>MARC21 supports two types of fields: reference fields and data fields.
 * Reference fields consist of a name tag and a single value. Data fields have a
 * name tag, two indicators and consist of subfields which have an identifier
 * each.
 * </p>
 *
 * <p>The {@code Marc21Encoder} encodes a stream as follows:
 * </p>
 *
 * <ul>
 *   <li>top-level literals are encoded as reference fields. Their name must match
 *   the requirements for reference field tags in ISO 2709:2008 records.</li>
 *
 *   <li>entities are encoded as data fields. Only one level of entities is
 *   supported. The entity name must consist of a three letter tag name followed
 *   by two indicator characters. The tag name must follow the requirements for
 *   data field tags in ISO 2709:2008 records.</li>
 *
 *   <li>Literals in entities are encoded as subfields. The literal name is used
 *   as subfield indicator and must therefore be a single character.</li>
 *
 *   <li>If a literal named "leader" is encountered it is treated as a ISO
 *   2709:2008 record label and some of its contents (record status,
 *   implementation codes, user system characters) are copied into the generated
 *   record</li>
 * </ul>
 *
 * <p>The stream expected by the encoder is compatible to the streams emitted by
 * the {@link MarcDecoder} and the {MarcXmlHandler}.
 * </p>
 *
 * <p>The record identifier in {@code startRecord} is ignored. To add an identifier
 * to the MARC21 record a reference field with tag name 001 need to be added.
 * </p>
 *
 * @throws FormatException
 *             if the stream cannot be converted into a MARC21 record.
 *
 * @author Christoph Böhme
 *
 */
@In(StreamReceiver.class)
@Out(String.class)
@Description("Encodes MARC21 records")
public final class Marc21Encoder extends
		DefaultStreamPipe<ObjectReceiver<String>> {

	public static final String LEADER_LITERAL = "leader";

	private static final RecordFormat MARC21 = new RecordFormat();

	private final RecordBuilder builder = new RecordBuilder(MARC21);
	private final int nameLength;

	private boolean inField;

	// CHECKSTYLE OFF: MagicNumber
	static {
		MARC21.setIndicatorLength(2);
		MARC21.setIdentifierLength(2);
		MARC21.setFieldLengthLength(4);
		MARC21.setFieldStartLength(5);
		MARC21.setImplDefinedPartLength(0);
	}
	// CHECKSTYLE ON: MagicNumber

	public Marc21Encoder() {
		super();
		nameLength = Iso2709Format.TAG_LENGTH + MARC21.getIndicatorLength();
	}

	@Override
	public void startRecord(final String identifier) {
		inField = false;
		builder.reset();
	}

	@Override
	public void endRecord() {
		getReceiver().process(builder.toString());
	}

	@Override
	public void startEntity(final String name) {
		if (name.length() != nameLength) {
			throw new FormatException("invalid entity name: " + name);
		}

		final String tag = name.substring(0, Iso2709Format.TAG_LENGTH);
		final String indicators = name.substring(Iso2709Format.TAG_LENGTH);
		builder.startField(tag, indicators);
		inField = true;
	}

	@Override
	public void endEntity() {
		inField = false;
		builder.endField();
	}

	@Override
	public void literal(final String name, final String value) {
		if (LEADER_LITERAL.equals(name)) {
			setRecordLabel(value);
		} else if (inField) {
			builder.appendSubfield(name, value);
		} else {
			builder.appendReferenceField(name, value);
		}
	}

	private void setRecordLabel(final String value) {
		if (value.length() != Iso2709Format.RECORD_LABEL_LENGTH) {
			throw new FormatException("leader must be 24 characters long");
		}
		builder.setRecordStatus(value.charAt(Iso2709Format.RECORD_STATUS_POS));
		builder.setImplCodes(value.substring(Iso2709Format.IMPL_CODES_START,
				Iso2709Format.IMPL_CODES_END));
		builder.setSystemChars(value.substring(
				Iso2709Format.SYSTEM_CHARS_START,
				Iso2709Format.SYSTEM_CHARS_END));
	}

}
