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

package org.metafacture.biblio.marc21;

import org.metafacture.biblio.iso2709.FieldHandler;
import org.metafacture.biblio.iso2709.Record;
import org.metafacture.biblio.iso2709.RecordFormat;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.FormatException;
import org.metafacture.framework.MissingIdException;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Decodes MARC 21 records into an event stream. This decoder only processes
 * single records. Input data containing multiple records must be split into
 * individual records before passing it to this decoder.
 * <p>
 * This decoder extracts the following parts from MARC 21 records:
 * <ul>
 *   <li>bibliographic information in the record leader,
 *   <li>record identifier,
 *   <li>control fields,
 *   <li>data fields.
 * </ul>
 * This decoder only supports MARC 21 records with UTF-8 encoding. Other
 * character coding schemes are not supported. A
 * {@link org.metafacture.framework.FormatException} is thrown if a record
 * with an unsupported coding scheme is encountered.
 * <p>
 * The bibliographic information in the record leader is
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
 * This information is emitted as an entity named
 * {@value org.metafacture.biblio.marc21.Marc21EventNames#LEADER_ENTITY}. It is
 * emitted directly after the <i>start-record</i> event. The entity contains
 * the following literals:
 * <ol>
 *   <li>{@value org.metafacture.biblio.marc21.Marc21EventNames#RECORD_STATUS_LITERAL}
 *   <li>{@value org.metafacture.biblio.marc21.Marc21EventNames#RECORD_TYPE_LITERAL}
 *   <li>{@value org.metafacture.biblio.marc21.Marc21EventNames#BIBLIOGRAPHIC_LEVEL_LITERAL}
 *   <li>{@value org.metafacture.biblio.marc21.Marc21EventNames#TYPE_OF_CONTROL_LITERAL}
 *   <li>{@value org.metafacture.biblio.marc21.Marc21EventNames#CHARACTER_CODING_LITERAL}
 *   <li>{@value org.metafacture.biblio.marc21.Marc21EventNames#ENCODING_LEVEL_LITERAL}
 *   <li>{@value org.metafacture.biblio.marc21.Marc21EventNames#CATALOGING_FORM_LITERAL}
 *   <li>{@value org.metafacture.biblio.marc21.Marc21EventNames#MULTIPART_LEVEL_LITERAL}
 * </ol>
 * The literals are emitted in the order in which they are listed here. The
 * values of these literals are the characters at the corresponding
 * positions in the record leader (see
 * <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
 * Standard: Record Leader</a> for a description of the allowed values). The
 * literal values are always only single characters. As this decoder only
 * supports MARC 21 records with UTF-8 encoding, the value of the literal
 * {@value org.metafacture.biblio.marc21.Marc21EventNames#CHARACTER_CODING_LITERAL}
 * will always be &quot;a&quot;.
 * <p>
 * For example, given a record with the leader
 * <pre>
 * 00128noa a2200073zu 4500
 * </pre>
 * the following event stream will be emitted:
 * <pre>
 * start-record &quot;1&quot;
 * start-entity {@value org.metafacture.biblio.marc21.Marc21EventNames#LEADER_ENTITY}
 * literal {@value org.metafacture.biblio.marc21.Marc21EventNames#RECORD_STATUS_LITERAL}: n
 * literal {@value org.metafacture.biblio.marc21.Marc21EventNames#RECORD_TYPE_LITERAL}: o
 * literal {@value org.metafacture.biblio.marc21.Marc21EventNames#BIBLIOGRAPHIC_LEVEL_LITERAL}: a
 * literal {@value org.metafacture.biblio.marc21.Marc21EventNames#TYPE_OF_CONTROL_LITERAL}: " "
 * literal {@value org.metafacture.biblio.marc21.Marc21EventNames#CHARACTER_CODING_LITERAL}: a
 * literal {@value org.metafacture.biblio.marc21.Marc21EventNames#ENCODING_LEVEL_LITERAL}: z
 * literal {@value org.metafacture.biblio.marc21.Marc21EventNames#CATALOGING_FORM_LITERAL}: u
 * literal {@value org.metafacture.biblio.marc21.Marc21EventNames#MULTIPART_LEVEL_LITERAL}: " "
 * end-entity
 * &hellip;
 * </pre>
 * The record identifier is taken from field &quot;001&quot;. It is used as
 * identifier in the <i>start-record</i> event. Additionally, it is emitted as
 * a control field (since that is what it is technically). The behaviour of
 * the decoder if a record has no identifier can be configured through the
 * {@link #setIgnoreMissingId(boolean)} parameter.
 * <p>
 * Control fields are emitted as literals with their tag as literal name and
 * their field value as literal value.
 * <p>
 * Data fields are emitted as entities. The entity name consists of the tag
 * followed by the two indicator characters of the field. For each sub field
 * in the data field a <i>literal</i> event is emitted. The literal name is
 * the identifier character of the sub field and the literal value is the data
 * value of the sub field.
 * <p>
 * All fields are emitted in the order in which they appear in the directory
 * of the MARC 21 record. For overlong fields which have multiple directory
 * entries (see section 4.4.4 in the ISO 2709:2008 standard), only the tag
 * from the first entry is used, the remaining ones are ignored.
 * <p>
 * Empty control fields and sub fields are emitted as literals with an empty
 * value. Data fields without sub fields produce only a <i>start-entity</i>
 * and an <i>end-entity</i> event without any <i>literal</i> events in-between.
 * If the decoder receives an empty input string it is ignored and no stream
 * events are emitted.
 * <p>
 * If an error occurs during decoding, a
 * {@link org.metafacture.framework.FormatException} is thrown.
 *
 * @author Christoph Böhme
 * @see "ISO 2709:2008 Standard"
 * @see <a href="http://www.loc.gov/marc/specifications/spechome.html">MARC-21
 * Standards</a>
 */
@In(String.class)
@Out(StreamReceiver.class)
@Description("Decodes MARC 21 records (UTF-8 encoding expected).")
@FluxCommand("decode-marc21")
public final class Marc21Decoder extends DefaultObjectPipe<String, StreamReceiver> {

    public static final boolean EMIT_LEADER_AS_WHOLE = false;
    public static final boolean IGNORE_MISSING_ID = false;

    private final FieldHandler fieldHandler = new Marc21Handler();

    private boolean ignoreMissingId = IGNORE_MISSING_ID;
    private boolean emitLeaderAsWhole = EMIT_LEADER_AS_WHOLE;

    /**
     * Creates an instance of {@link Marc21Decoder}.
     */
    public Marc21Decoder() {
    }

    /**
     * Controls whether the decoder aborts processing if a record has no identifier.
     * A {@link MissingIdException} is thrown in these cases. If this parameter is
     * set to true then the identifier emitted with the <i>start-record</i> event of
     * records without field &quot;001&quot; will be an empty string.
     * <p>
     * <strong>Default value: {@value #IGNORE_MISSING_ID}</strong>
     * <p>
     * This parameter can be changed anytime during processing. The new value
     * becomes effective with the next record being processed.
     *
     * @param ignoreMissingId true if missing identifiers should be silently
     *                        ignored.
     */
    public void setIgnoreMissingId(final boolean ignoreMissingId) {
        this.ignoreMissingId = ignoreMissingId;
    }

    /**
     * Gets the flag to decide whether to abort the processing of a record if it has
     * no identifier.
     *
     * @return true if a missing identifier shouldn't abort processing, otherwise
     *         false
     */
    public boolean getIgnoreMissingId() {
        return ignoreMissingId;
    }

    /**
     * Controls whether the Record Leader should be emitted as a whole instead of
     * extracting the bibliographic information in the record leader.
     *
     * <strong>Default value: {@value #EMIT_LEADER_AS_WHOLE}</strong>
     *
     * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
     *      Standard: Record Leader</a>
     * @param emitLeaderAsWhole true if the leader should be emitted as a whole.
     */
    public void setEmitLeaderAsWhole(final boolean emitLeaderAsWhole) {
        this.emitLeaderAsWhole = emitLeaderAsWhole;
    }

    /**
     * Gets the flag to decide whether the Record Leader is emitted as whole instead
     * of extracting the bibliographic information in the record leader.
     *
     * @return true if the Record Leader is emitted as whole, otherwise false
     */
    public boolean getEmitLeaderAsWhole() {
        return emitLeaderAsWhole;
    }

    @Override
    public void process(final String obj) {
        if (obj.isEmpty()) {
            return;
        }
        final Record record = new Record(obj.getBytes(Marc21Constants.MARC21_CHARSET));
        record.setCharset(Marc21Constants.MARC21_CHARSET);

        requireMarc21RecordFormat(record.getRecordFormat());
        requireUTF8Encoding(record);

        getReceiver().startRecord(tryGetRecordId(record));
        emitLeader(record);
        record.processFields(fieldHandler);
        getReceiver().endRecord();
    }

    private void requireMarc21RecordFormat(final RecordFormat format) {
        if (!Marc21Constants.MARC21_FORMAT.equals(format)) {
            throw new FormatException("invalid record format. Expected " +
                    Marc21Constants.MARC21_FORMAT + " but got " + format);
        }
    }

    private void requireUTF8Encoding(final Record record) {
        if (record.getImplCodes()[Marc21Constants.CHARACTER_CODING_INDEX] != 'a') {
            throw new FormatException(
                    "invalid record encoding. Only UTF-8 is supported");
        }
    }

    private String tryGetRecordId(final Record record) {
        final String id = record.getRecordId();
        if (id == null) {
            if (!ignoreMissingId) {
                throw new MissingIdException("record has no id");
            }
            return "";
        }
        return id;
    }

    private void emitLeader(final Record record) {
        getReceiver().startEntity(Marc21EventNames.LEADER_ENTITY);
        if (emitLeaderAsWhole) {
            getReceiver().literal(Marc21EventNames.LEADER_ENTITY, record.getLabel());
        }
        else {
            final char[] implCodes = record.getImplCodes();
            final char[] systemChars = record.getSystemChars();
            getReceiver().literal(Marc21EventNames.RECORD_STATUS_LITERAL, String.valueOf(
                        record.getRecordStatus()));
            getReceiver().literal(Marc21EventNames.RECORD_TYPE_LITERAL, String.valueOf(
                        implCodes[Marc21Constants.RECORD_TYPE_INDEX]));
            getReceiver().literal(Marc21EventNames.BIBLIOGRAPHIC_LEVEL_LITERAL, String.valueOf(
                        implCodes[Marc21Constants.BIBLIOGRAPHIC_LEVEL_INDEX]));
            getReceiver().literal(Marc21EventNames.TYPE_OF_CONTROL_LITERAL, String.valueOf(
                        implCodes[Marc21Constants.TYPE_OF_CONTROL_INDEX]));
            getReceiver().literal(Marc21EventNames.CHARACTER_CODING_LITERAL, String.valueOf(
                        implCodes[Marc21Constants.CHARACTER_CODING_INDEX]));
            getReceiver().literal(Marc21EventNames.ENCODING_LEVEL_LITERAL, String.valueOf(
                        systemChars[Marc21Constants.ENCODING_LEVEL_INDEX]));
            getReceiver().literal(Marc21EventNames.CATALOGING_FORM_LITERAL, String.valueOf(
                        systemChars[Marc21Constants.CATALOGING_FORM_INDEX]));
            getReceiver().literal(Marc21EventNames.MULTIPART_LEVEL_LITERAL, String.valueOf(
                        systemChars[Marc21Constants.MULTIPART_LEVEL_INDEX]));
        }
        getReceiver().endEntity();
    }

    /**
     * Emits the fields in a MARC 21 record as stream events.
     */
    private final class Marc21Handler implements FieldHandler {

        Marc21Handler() {
        }

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
            final int nameLength = tag.length + indicators.length;
            final char[] name = new char[nameLength];
            System.arraycopy(tag, 0, name, 0, tag.length);
            System.arraycopy(indicators, 0, name, tag.length, indicators.length);
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
