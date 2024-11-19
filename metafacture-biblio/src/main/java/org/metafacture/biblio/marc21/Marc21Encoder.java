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

import org.metafacture.biblio.iso2709.Iso2709Constants;
import org.metafacture.biblio.iso2709.RecordBuilder;
import org.metafacture.biblio.iso2709.RecordFormat;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.FormatException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

import java.util.Arrays;

/**
 * Encodes a stream in MARC21 format.
 * <p>
 * MARC21 supports two types of fields: reference fields and data fields.
 * Reference fields consist of a name tag and a single value. Data fields have a
 * name tag, two indicators and consist of subfields which have an identifier
 * each.
 * <p>
 * The {@code Marc21Encoder} encodes a stream as follows:
 * <ul>
 *   <li>Top-level literals are encoded as reference fields. Their name must
 *   match the requirements for reference field tags in ISO 2709:2008 records.
 *
 *   <li>Entities are encoded as data fields. Only one level of entities is
 *   supported. The entity name must consist of a three letter tag name followed
 *   by two indicator characters. The tag name must follow the requirements for
 *   data field tags in ISO 2709:2008 records.
 *
 *   <li>Literals in entities are encoded as subfields. The literal name is used
 *   as subfield indicator and must therefore be a single character.
 *
 *   <li>Top level literals named &quot;type&quot;, which may be produced by
 *   {@link MarcXmlHandler} are ignored.
 * </ul>
 * The stream expected by the encoder is compatible to the streams emitted by
 * the {@link Marc21Decoder} and the {@link MarcXmlHandler}.
 * <p>
 * The record identifier in {@code startRecord} is ignored by default. The
 * event stream is expected to contain  a <i>literal</i> event named
 * &quot;001&quot; with the record id. Alternatively, setting
 * {@link #setGenerateIdField(boolean)} to true enables generation of a record
 * identifier field from the record id.
 *
 * @author Christoph Böhme
 *
 */
@In(StreamReceiver.class)
@Out(String.class)
@Description("Encodes MARC21 records")
@FluxCommand("encode-marc21")
public final class Marc21Encoder extends
        DefaultStreamPipe<ObjectReceiver<String>> {

    private static final int NAME_LENGTH = RecordFormat.TAG_LENGTH +
            Marc21Constants.MARC21_FORMAT.getIndicatorLength();

    private final RecordBuilder builder;

    private State state = State.IN_STREAM;

    private boolean generateIdField;
    private boolean validateLeader = true;

    /**
     * Initializes the encoder with MARC 21 constants and charset.
     */
    public Marc21Encoder() {
        builder = new RecordBuilder(Marc21Constants.MARC21_FORMAT);
        builder.setCharset(Marc21Constants.MARC21_CHARSET);
    }

    /**
     * Controls whether the record identifier field (&quot;001&quot;) is
     * generated from the record id in the <i>start-record</i> event. If id field
     * generation is enabled the event stream should not contain a <i>literal
     * &quot;001&quot;</i> event as this would result in two record identifier
     * fields being written into the MARC 21 record.
     * <p>
     * The default value of {@code generatedFieldId} is false.
     * <p>
     * The parameter can be changed at anytime. It becomes effective with the
     * next record being processed.
     *
     * @param generateIdField if true a record identifier field is generated.
     */
    public void setGenerateIdField(final boolean generateIdField) {
        this.generateIdField = generateIdField;
    }

    /**
     * Controls whether the leader should be validated.
     * <p>
     * The default value of {@code validateLeader} is true.
     * <p>
     *
     * @param validateLeader if false the leader is not validated
     */
    public void setValidateLeader(final boolean validateLeader) {
        this.validateLeader = validateLeader;
    }

    /**
     * Gets the flag to decide whether the ID field is generated.
     *
     * @return true if the record ID is generated, otherwise false
     */
    public boolean getGenerateIdField() {
        return generateIdField;
    }

    @Override
    public void startRecord(final String identifier) {
        builder.reset();
        initLeader();
        if (generateIdField) {
            builder.appendIdentifierField(identifier);
        }
        state = State.IN_RECORD;
    }

    private void initLeader() {
        builder.setRecordStatus(' ');
        builder.setImplCodes(new char[]{' ', ' ', ' ', ' '});
        builder.setSystemChars(new char[]{' ', ' ', ' '});
        builder.setReservedChar(Marc21Constants.RESERVED_CHAR);
    }

    @Override
    public void endRecord() {
        final byte[] record = builder.build();
        getReceiver().process(new String(record, Marc21Constants.MARC21_CHARSET));
        state = State.IN_STREAM;
    }

    @Override
    public void startEntity(final String name) {
        if (state != State.IN_RECORD) {
            throw new IllegalStateException("only top level entities are allowed");
        }
        if (Marc21EventNames.LEADER_ENTITY.equals(name)) {
            state = State.IN_LEADER_ENTITY;
        }
        else {
            startField(name);
            state = State.IN_FIELD_ENTITY;
        }
    }

    private void startField(final String name) {
        if (name.length() != NAME_LENGTH) {
            throw new FormatException("invalid leader entity name: " + name);
        }
        final char[] tag = new char[RecordFormat.TAG_LENGTH];
        final char[] indicators = new char[Marc21Constants.MARC21_FORMAT.getIndicatorLength()];
        name.getChars(0, tag.length, tag, 0);
        name.getChars(tag.length, name.length(), indicators, 0);
        builder.startDataField(tag, indicators);
    }

    @Override
    public void endEntity() {
        if (state.equals(State.IN_FIELD_ENTITY)) {
            builder.endDataField();
        }
        state = State.IN_RECORD;
    }

    @Override
    public void literal(final String name, final String value) {
        switch (state) {
            case IN_FIELD_ENTITY:
                builder.appendSubfield(name.toCharArray(), value);
                break;
            case IN_LEADER_ENTITY:
                if (name == Marc21EventNames.LEADER_ENTITY) {
                    processLeaderAsOneLiteral(value);
                }
                else {
                    processLeaderAsSubfields(name, value);
                }
                break;
            case IN_RECORD:
                processTopLevelLiteral(name, value);
                break;
            default:
                throw new AssertionError("unknown or unexpected state: " + state);
        }
    }

    private void processLeaderAsOneLiteral(final String value) {
        if (value.length() != Iso2709Constants.RECORD_LABEL_LENGTH) {
            throw new FormatException(
                    "leader literal must contain " + Iso2709Constants.RECORD_LABEL_LENGTH + "  characters: " + value);
        }
        processLeaderAsSubfields(Marc21EventNames.RECORD_STATUS_LITERAL, value.charAt(Iso2709Constants.RECORD_STATUS_POS));
        processLeaderAsSubfields(Marc21EventNames.RECORD_TYPE_LITERAL, value.charAt(Iso2709Constants.IMPL_CODES_START));
        processLeaderAsSubfields(Marc21EventNames.BIBLIOGRAPHIC_LEVEL_LITERAL, value.charAt(Iso2709Constants.IMPL_CODES_START + 1));
        processLeaderAsSubfields(Marc21EventNames.TYPE_OF_CONTROL_LITERAL, value.charAt(Iso2709Constants.IMPL_CODES_START + 2));
        processLeaderAsSubfields(Marc21EventNames.CHARACTER_CODING_LITERAL, value.charAt(Iso2709Constants.RECORD_STATUS_POS + Iso2709Constants.IMPL_CODES_LENGTH));
        processLeaderAsSubfields(Marc21EventNames.ENCODING_LEVEL_LITERAL, value.charAt(Iso2709Constants.SYSTEM_CHARS_START));
        processLeaderAsSubfields(Marc21EventNames.CATALOGING_FORM_LITERAL, value.charAt(Iso2709Constants.SYSTEM_CHARS_START + 1));
        processLeaderAsSubfields(Marc21EventNames.MULTIPART_LEVEL_LITERAL, value.charAt(Iso2709Constants.SYSTEM_CHARS_START + 2));
    }

    private void processLeaderAsSubfields(final String name, final String value) {
        if (value.length() != 1) {
            throw new FormatException(
                    "leader literal must only contain a single character:" + name);
        }
        processLeaderAsSubfields(name, value.charAt(0));
    }

    private void processLeaderAsSubfields(final String name, final char code) {
        switch (name) {
            case Marc21EventNames.RECORD_STATUS_LITERAL:
                requireValidCode(code, Marc21Constants.RECORD_STATUS_CODES);
                builder.setRecordStatus(code);
                break;
            case Marc21EventNames.RECORD_TYPE_LITERAL:
                requireValidCode(code, Marc21Constants.RECORD_TYPE_CODES);
                builder.setImplCode(Marc21Constants.RECORD_TYPE_INDEX, code);
                break;
            case Marc21EventNames.BIBLIOGRAPHIC_LEVEL_LITERAL:
                requireValidCode(code, Marc21Constants.BIBLIOGRAPHIC_LEVEL_CODES);
                builder.setImplCode(Marc21Constants.BIBLIOGRAPHIC_LEVEL_INDEX, code);
                break;
            case Marc21EventNames.TYPE_OF_CONTROL_LITERAL:
                requireValidCode(code, Marc21Constants.TYPE_OF_CONTROL_CODES);
                builder.setImplCode(Marc21Constants.TYPE_OF_CONTROL_INDEX, code);
                break;
            case Marc21EventNames.CHARACTER_CODING_LITERAL:
                requireValidCode(code, Marc21Constants.CHARACTER_CODING_CODES);
                builder.setImplCode(Marc21Constants.CHARACTER_CODING_INDEX, code);
                break;
            case Marc21EventNames.ENCODING_LEVEL_LITERAL:
                requireValidCode(code, Marc21Constants.ENCODING_LEVEL_CODES);
                builder.setSystemChar(Marc21Constants.ENCODING_LEVEL_INDEX, code);
                break;
            case Marc21EventNames.CATALOGING_FORM_LITERAL:
                requireValidCode(code, Marc21Constants.CATALOGING_FORM_CODES);
                builder.setSystemChar(Marc21Constants.CATALOGING_FORM_INDEX, code);
                break;
            case Marc21EventNames.MULTIPART_LEVEL_LITERAL:
                requireValidCode(code, Marc21Constants.MULTIPART_LEVEL_CODES);
                builder.setSystemChar(Marc21Constants.MULTIPART_LEVEL_INDEX, code);
                break;
            default:
                throw new FormatException("unknown literal in leader entity: " + name);
        }
    }

    private void requireValidCode(final char code, final char[] validCodes) {
        if (validateLeader) {
            for (final char validCode : validCodes) {
                if (validCode == code) {
                    return;
                }
            }
            throw new FormatException("invalid code in leader'" + code + "'; allowed codes are: " + Arrays.toString(validCodes));
        }
    }

    private void processTopLevelLiteral(final String name, final String value) {
        if (Marc21EventNames.MARCXML_TYPE_LITERAL.equals(name)) {
            // MarcXmlHandler may output `type` literals. The
            // information in these literals is not included in
            // marc21 records. Therefore, we need to ignore
            // these literals here.
            return;
        }
        if (Marc21EventNames.LEADER_ENTITY.equals(name)) {
            processLeaderAsOneLiteral(value);
        }
        else {
            builder.appendReferenceField(name.toCharArray(), value);
        }
    }

    @Override
    protected void onResetStream() {
        builder.reset();
        state = State.IN_STREAM;
    }

    private enum State {
        IN_STREAM, IN_RECORD, IN_FIELD_ENTITY, IN_LEADER_ENTITY
    }

}
