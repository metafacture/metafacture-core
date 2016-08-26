/*
 *  Copyright 2016 Christoph Böhme
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

import java.nio.charset.Charset;

import org.culturegraph.mf.exceptions.FormatException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.iso2709.FieldHandler;
import org.culturegraph.mf.iso2709.Label;
import org.culturegraph.mf.iso2709.Record;
import org.culturegraph.mf.iso2709.RecordFormat;

/**
 * Decodes MARC 21 records into an event stream. This decoder only processes
 * single records. Input data containing multiple records must be split into
 * individual records before passing it to this decoder.
 * <p>
 * This decoder extracts the following parts from MARC 21 records:
 * <ul>
 *   <li>record leader,
 *   <li>record identifier,
 *   <li>control fields,
 *   <li>data fields.
 * </ul>
 * This decoder only supports MARC 21 records with UTF-8 encoding. Other
 * character coding schemes are not supported. A {@link FormatException} is
 * thrown if a record with an unsupported coding scheme is encountered.
 * <p>
 * Depending on the {@link #setSplitLeader(boolean)} parameter, the MARC 21
 * record leader is either emitted as a single literal or split into its
 * components and emitted as an entity. The name of the literal or entity is
 * &quot;{@value #LEADER_NAME}&quot; in both cases. The events describing the
 * record leader are emitted as the first events after the
 * <i>start-record</i> event. See {@link #setSplitLeader(boolean)} for an
 * in-depth description of the event stream emitted for the leader.
 * <p>
 * The record identifier is taken from field &quot;001&quot;. It is used as
 * identifier in the <i>start-record</i> event. Additionally, it is emitted
 * as a control field (since that is what it is technically). The behaviour
 * of the decoder if a record has no identifier can be configured through the
 * {@link #setIgnoreMissingId(boolean)} parameter.
 * <p>
 * Control fields are emitted as literals with their tag as literal name and
 * their field value as literal value.
 * <p>
 * Data fields are emitted as entities. The entity name consists of the tag
 * followed by the two indicator characters of the field. For each sub field
 * in the data field a <i>literal</i> event is emitted. The literal name is
 * the identifier character of the sub field and the literal value is the
 * data value of the sub field.
 * <p>
 * All fields are emitted in the order in which they appear in the directory
 * of the MARC 21 record. For overlong fields which have multiple directory
 * entries (see section 4.4.4 in the ISO 2709:2008 standard), only the tag
 * from the first entry is used, the remaining ones are ignored.
 * <p>
 * Empty control fields and sub fields are emitted as literals with an empty
 * value. Data fields without sub fields produce only a <i>start-entity</i>
 * and <i>end-entity</i> event without any <i>literal</i> events in-between.
 * <p>
 * If an error occurs during decoding, a {@link FormatException} is thrown.
 *
 * @author Christoph Böhme
 * @see "ISO 2709:2008 Standard"
 * @see <a href="http://www.loc.gov/marc/specifications/spechome.html">MARC-21
 * Standards</a>
 *
 */
@In(String.class)
@Out(StreamReceiver.class)
@Description("Decodes MARC 21 records")
public final class Marc21Decoder
		extends DefaultObjectPipe<String, StreamReceiver> {

	/**
	 * Name of the <i>literal</i> or <i>entity</i> event which contains the
	 * record leader.
	 *
	 * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
	 * Standard: Record Leader</a>
	 */
	public static final String LEADER_NAME = "leader";

	/**
	 * Name of the <i>literal</i> event emitted for the record status field in
	 * split mode (see {@link #setSplitLeader(boolean)}.
	 * <p>
	 * The name of the literal is &quot;{@value #RECORD_STATUS_LITERAL}&quot;.
	 * <p>
	 * The record status is specified at position 5 in the record leader.
	 *
	 * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
	 * Standard: Record Leader</a>
	 */
	public static final String RECORD_STATUS_LITERAL = "status";

	/**
	 * Name of the <i>literal</i> event emitted for the type of record field in
	 * split mode (see {@link #setSplitLeader(boolean)}.
	 * <p>
	 * The name of the literal is &quot;{@value #RECORD_TYPE_LITERAL}&quot;.
	 * <p>
	 * The type of record is specified at position 6 in the record leader.
	 *
	 * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
	 * Standard: Record Leader</a>
	 */
	public static final String RECORD_TYPE_LITERAL = "type";

	/**
	 * Name of the <i>literal</i> event emitted for the bibliographic level
	 * field in split mode (see {@link #setSplitLeader(boolean)}.
	 * <p>
	 * The name of the literal is &quot;{@value
	 * #BIBLIOGRAPHIC_LEVEL_LITERAL}&quot;.
	 * <p>
	 * The bibliographic level is specified at position 7 in the record leader.
	 *
	 * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
	 * Standard: Record Leader</a>
	 */
	public static final String BIBLIOGRAPHIC_LEVEL_LITERAL = "bibliographicLevel";

	/**
	 * Name of the <i>literal</i> event emitted for the type of control field in
	 * split mode (see {@link #setSplitLeader(boolean)}.
	 * <p>
	 * The name of the literal is &quot;{@value #TYPE_OF_CONTROL_LITERAL}&quot;.
	 * <p>
	 * The type of control is specified at position 8 in the record leader.
	 *
	 * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
	 * Standard: Record Leader</a>
	 */
	public static final String TYPE_OF_CONTROL_LITERAL = "typeOfControl";

	/**
	 * Name of the <i>literal</i> event emitted for the character coding scheme
	 * field in split mode (see {@link #setSplitLeader(boolean)}.
	 * <p>
	 * The name of the literal is &quot;{@value #CHARACTER_CODING_LITERAL}&quot;.
	 * <p>
	 * The character coding scheme is specified at position 9 in the record
	 * leader.
	 *
	 * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
	 * Standard: Record Leader</a>
	 */
	public static final String CHARACTER_CODING_LITERAL = "characterCodingScheme";

	/**
	 * Name of the <i>literal</i> event emitted for the encoding level field in
	 * split mode (see {@link #setSplitLeader(boolean)}.
	 * <p>
	 * The name of the literal is &quot;{@value #ENCODING_LEVEL_LITERAL}&quot;.
	 * <p>
	 * The encoding level is specified at position 17 in the record leader.
	 *
	 * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
	 * Standard: Record Leader</a>
	 */
	public static final String ENCODING_LEVEL_LITERAL = "encodingLevel";

	/**
	 * Name of the <i>literal</i> event emitted for the descriptive cataloging
	 * form field in split mode (see {@link #setSplitLeader(boolean)}.
	 * <p>
	 * The name of the literal is &quot;{@value #CATALOGING_FORM_LITERAL}&quot;.
	 * <p>
	 * The descriptive cataloging form is specified at position 18 in the record
	 * leader.
	 *
	 * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
	 * Standard: Record Leader</a>
	 */
	public static final String CATALOGING_FORM_LITERAL = "catalogingForm";

	/**
	 * Name of the <i>literal</i> event emitted for the multipart resource
	 * record level field in split mode (see {@link #setSplitLeader(boolean)}.
	 * <p>
	 * The name of the literal is &quot;{@value #MULTIPART_LEVEL_LITERAL}&quot;.
	 * <p>
	 * The multipart resource record level is specified at position 19 in the
	 * record leader.
	 *
	 * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
	 * Standard: Record Leader</a>
	 */
	public static final String MULTIPART_LEVEL_LITERAL = "multipartLevel";

	private static final Charset MARC21_CHARSET = Charset.forName("UTF-8");

	private static final RecordFormat MARC_FORMAT = new RecordFormat();
	static {
		MARC_FORMAT.setIndicatorLength(2);
		MARC_FORMAT.setIdentifierLength(2);
		MARC_FORMAT.setFieldLengthLength(4);
		MARC_FORMAT.setFieldStartLength(5);
		MARC_FORMAT.setImplDefinedPartLength(0);
	}

	private static final int RECORD_TYPE_INDEX = 0;
	private static final int BIBLIOGRAPHIC_LEVEL_INDEX = 1;
	private static final int TYPE_OF_CONTROL_INDEX = 2;
	private static final int CHARACTER_CODING_INDEX = 3;
	private static final int ENCODING_LEVEL_INDEX = 0;
	private static final int CATALOGING_FORM_INDEX = 1;
	private static final int MULTIPART_LEVEL_INDEX = 2;

	private final FieldHandler fieldHandler = new Marc21Handler();

	private boolean splitLeader;
	private boolean ignoreMissingId;

	/**
	 * Controls whether the full record leader is emitted as a single literal or
	 * split into its parts and emitted as an entity. If the leader is split,
	 * only the parts of the leader carrying bibliographic information are
	 * emitted. These are
	 * <ul>
	 *   <li>record status,
	 *   <li>record type,
	 *   <li>bibliographic level,
	 *   <li>type of control,
	 *   <li>character coding scheme,
	 *   <li>encoding level,
	 *   <li>descriptive cataloging form,
	 *   <li>multipart resource record level.
	 * </ul>
	 * The parts of the leader describing the lengths of the different elements
	 * in the record are not emitted in splitting mode.
	 * <p>
	 * If splitting of the leader is not enabled, all 24 characters of the leader
	 * are emitted as the value of a single literal named {@value #LEADER_NAME}.
	 * This will always be the first <i>literal</i> event after the
	 * <i>start-record</i> event.
	 * <p>
	 * For example, given a record with the leader
	 * <pre>
	 * 00128noa a2200073zu 4500
	 * </pre>
	 * the following sequence of events is produced if not splitting is disabled:
	 * <pre>
	 * start-record &quot;1&quot;
	 * literal &quot;{@value #LEADER_NAME}&quot;: 00128noa a2200073zu 4500
	 * &hellip;
	 * </pre>
	 *
	 * If splitting of the leader is enabled, an entity named {@value
	 * #LEADER_NAME} is emitted directly after the <i>start-record</i> event.
	 * Within this entity a literal is emitted for each of the items in the list
	 * above. The names of the literals are
	 * <ol>
	 *   <li>{@value #RECORD_STATUS_LITERAL}
	 *   <li>{@value #RECORD_TYPE_LITERAL}
	 *   <li>{@value #BIBLIOGRAPHIC_LEVEL_LITERAL}
	 *   <li>{@value #TYPE_OF_CONTROL_LITERAL}
	 *   <li>{@value #CHARACTER_CODING_LITERAL}
	 *   <li>{@value #ENCODING_LEVEL_LITERAL}
	 *   <li>{@value #CATALOGING_FORM_LITERAL}
	 *   <li>{@value #MULTIPART_LEVEL_LITERAL}
	 * </ol>
	 * The literals are emitted in the order in which they are listed here. The
	 * values of these literals are the characters at the corresponding
	 * positions in the record leader (see
	 * <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
	 * Standard: Record Leader</a> for a description of the positions and the
	 * allowed values). The literal values are always only single characters.
	 * <p>
	 * When decoding the example from above, the decoder will emit the following
	 * event stream in splitting mode:
	 * <pre>
	 * start-record &quot;1&quot;
	 * start-entity &quot;{@value #LEADER_NAME}&quot;
	 * literal &quot;{@value #RECORD_STATUS_LITERAL}&quot;: n
	 * literal &quot;{@value #RECORD_TYPE_LITERAL}&quot;: o
	 * literal &quot;{@value #BIBLIOGRAPHIC_LEVEL_LITERAL}&quot;: a
	 * literal &quot;{@value #TYPE_OF_CONTROL_LITERAL}&quot;: " "
	 * literal &quot;{@value #CHARACTER_CODING_LITERAL}&quot;: a
	 * literal &quot;{@value #ENCODING_LEVEL_LITERAL}&quot;: z
	 * literal &quot;{@value #CATALOGING_FORM_LITERAL}&quot;: u
	 * literal &quot;{@value #MULTIPART_LEVEL_LITERAL}&quot;: " "
	 * end-entity
	 * &hellip;
	 * </pre>
	 * As this decoder only supports MARC 21 records with UTF-8 encoding, the
	 * value of the <i>literal &quot;{@value #CHARACTER_CODING_LITERAL}&quot;
	 * </i> will always be &quot;a&quot;.
	 * <p>
	 * The default value of {@code splitLeader} is false.
	 * <p>
	 * This parameter can be changed anytime during processing. The new value
	 * becomes effective with the next record being processed.
	 *
	 * @param splitLeader
	 *            true if the leader should be split and wrapped in an entity.
	 *
	 * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC
	 * 21 Standards: Record Leader</a>
	 */
	public void setSplitLeader(final boolean splitLeader) {
		this.splitLeader = splitLeader;
	}

	public boolean getSplitLeader() {
		return splitLeader;
	}

	/**
	 * Controls whether the decoder aborts processing if a record has no
	 * identifier. If set to true then the identifier emitted with the
	 * <i>start-record</i> event of records without field &quot;001&quot; will
	 * be an empty string.
	 * <p>
	 * The default value of {@code ignoreMissingId} is false.
	 * <p>
	 * This parameter can be changed anytime during processing. The new value
	 * becomes effective with the next record being processed.
	 *
	 * @param ignoreMissingId
	 *            true if missing identifiers should be silently ignored.
	 */
	public void setIgnoreMissingId(final boolean ignoreMissingId) {
		this.ignoreMissingId = ignoreMissingId;
	}

	public boolean getIgnoreMissingId() {
		return ignoreMissingId;
	}

	@Override
	public void process(final String obj) {
		if (obj.isEmpty()) {
			return;
		}
		final Record record = new Record(obj.getBytes(MARC21_CHARSET));
		record.setCharset(MARC21_CHARSET);

		requireMarc21RecordFormat(record.getLabel().getRecordFormat());
		requireUTF8Encoding(record.getLabel());

		getReceiver().startRecord(tryGetRecordId(record));
		emitLeader(record);
		record.processFields(fieldHandler);
		getReceiver().endRecord();
	}

	private void requireMarc21RecordFormat(final RecordFormat format) {
		if (!MARC_FORMAT.equals(format)) {
			throw new FormatException("Invalid record format. Expected " +
					MARC_FORMAT + " but got " + format);
		}
	}

	private void requireUTF8Encoding(final Label label) {
		if (label.getImplCodes()[CHARACTER_CODING_INDEX] != 'a') {
			throw new FormatException(
					"Invalid record encoding. Only UTF-8 is supported");
		}
	}

	private String tryGetRecordId(final Record record) {
		final String id = record.getIdentifier();
		if (id == null) {
			if (!ignoreMissingId) {
				throw new MissingIdException("Record has no id");
			}
			return "";
		}
		return id;
	}

	private void emitLeader(final Record record) {
		if (splitLeader) {
			emitLeaderAsEntity(record);
		} else {
			getReceiver().literal(LEADER_NAME, record.getLabel().toString());
		}
	}

	private void emitLeaderAsEntity(final Record record) {
		final char[] implCodes = record.getLabel().getImplCodes();
		final char[] systemChars = record.getLabel().getSystemChars();
		getReceiver().startEntity(LEADER_NAME);
		getReceiver().literal(RECORD_STATUS_LITERAL,
				String.valueOf(record.getLabel().getRecordStatus()));
		getReceiver().literal(RECORD_TYPE_LITERAL,
				String.valueOf(implCodes[RECORD_TYPE_INDEX]));
		getReceiver().literal(BIBLIOGRAPHIC_LEVEL_LITERAL,
				String.valueOf(implCodes[BIBLIOGRAPHIC_LEVEL_INDEX]));
		getReceiver().literal(TYPE_OF_CONTROL_LITERAL,
				String.valueOf(implCodes[TYPE_OF_CONTROL_INDEX]));
		getReceiver().literal(CHARACTER_CODING_LITERAL,
				String.valueOf(implCodes[CHARACTER_CODING_INDEX]));
		getReceiver().literal(ENCODING_LEVEL_LITERAL,
				String.valueOf(systemChars[ENCODING_LEVEL_INDEX]));
		getReceiver().literal(CATALOGING_FORM_LITERAL,
				String.valueOf(systemChars[CATALOGING_FORM_INDEX]));
		getReceiver().literal(MULTIPART_LEVEL_LITERAL,
				String.valueOf(systemChars[MULTIPART_LEVEL_INDEX]));
		getReceiver().endEntity();
	}

	/**
	 * Emits the fields in a MARC 21 record as stream events.
	 */
	private final class Marc21Handler implements FieldHandler {

		@Override
		public void referenceField(final char[] tag, final char[] implDefinedPart,
				final String value) {
			getReceiver().literal(String.valueOf(tag), value);
		}

		@Override
		public void startDataField(final char[] tag, final char[] implDefinedPart,
				final char[] indicators) {
			getReceiver().startEntity(buildName(tag, indicators));
		}

		private String buildName(final char[] tag, final char[] indicators) {
			final char[] name = new char[5];
			name[0] = tag[0];
			name[1] = tag[1];
			name[2] = tag[2];
			name[3] = indicators[0];
			name[4] = indicators[1];
			return String.valueOf(name);
		}

		@Override
		public void endDataField() {
			getReceiver().endEntity();
		}

		@Override
		public void additionalImplDefinedPart(final char[] implDefinedPart) {
			// Nothing to do. MARC 21 does not use implementation defined parts.
		}

		@Override
		public void data(final char[] identifier, final String value) {
			getReceiver().literal(String.valueOf(identifier[0]), value);
		}

	}

}
