/* Copyright 2019 Pascal Christoph (hbz) and others
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

import org.metafacture.commons.XmlUtil;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

import java.util.Collections;

/**
 * Encodes a stream into MARCXML.
 *
 * @author some Jan (Eberhardt) did almost all
 * @author Pascal Christoph (dr0i) dug it up again
 */

@Description("Encodes a stream into MARCXML.")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("encode-marcxml")
public final class MarcXmlEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

    public static final String DEFAULT_ATTRIBUTE_MARKER = null;
    private static final String ATTRIBUTE_TEMPLATE = " %s=\"%s\"";

    private static final String ROOT_OPEN = "<marc:collection xmlns:marc=\"http://www.loc.gov/MARC21/slim\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\">";
    private static final String ROOT_CLOSE = "</marc:collection>";

    private static final String RECORD_OPEN = "<marc:record>";
    private static final String RECORD_CLOSE = "</marc:record>";

    private static final String CONTROLFIELD_OPEN_TEMPLATE = "<marc:controlfield tag=\"%s\">";
    private static final String CONTROLFIELD_CLOSE = "</marc:controlfield>";

    private static final String DATAFIELD_OPEN_TEMPLATE = "<marc:datafield tag=\"%s\" ind1=\"%s\" ind2=\"%s\">";
    private static final String DATAFIELD_CLOSE = "</marc:datafield>";

    private static final String SUBFIELD_OPEN_TEMPLATE = "<marc:subfield code=\"%s\">";
    private static final String SUBFIELD_CLOSE = "</marc:subfield>";

    private static final String LEADER_OPEN_TEMPLATE = "<marc:leader>";
    private static final String LEADER_CLOSE_TEMPLATE = "</marc:leader>";

    private static final String NEW_LINE = "\n";
    private static final String INDENT = "\t";

    private static final String XML_DECLARATION_TEMPLATE = "<?xml version=\"%s\" encoding=\"%s\"?>";

    private static final int LEADER_ENTITY_LENGTH = 5;

    private static final int IND1_BEGIN = 3;
    private static final int IND1_END = 4;
    private static final int IND2_BEGIN = 4;
    private static final int IND2_END = 5;
    private static final int TAG_BEGIN = 0;
    private static final int TAG_END = 3;

    private final StringBuilder builder;

    private boolean atStreamStart;

    private boolean omitXmlDeclaration;
    private String xmlVersion;
    private String xmlEncoding;

    private String currentEntity;
    private int indentationLevel;
    private boolean formatted;

    private String attributeMarker = DEFAULT_ATTRIBUTE_MARKER;
    private int recordAttributeOffset;

    public MarcXmlEncoder() {
        this.builder = new StringBuilder();
        this.atStreamStart = true;

        this.omitXmlDeclaration = false;
        this.xmlVersion = "1.0";
        this.xmlEncoding = "UTF-8";

        this.currentEntity = "";

        this.indentationLevel = 0;
        this.formatted = true;
    }

    public void omitXmlDeclaration(final boolean currentOmitXmlDeclaration) {
        omitXmlDeclaration = currentOmitXmlDeclaration;
    }

    public void setXmlVersion(final String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public void setXmlEncoding(final String xmlEncoding) {
        this.xmlEncoding = xmlEncoding;
    }

    /**
     * Formats the resulting xml, by indentation.
     *
     * @param formatted
     *            True, if formatting is activated.
     */
    public void setFormatted(final boolean formatted) {
        this.formatted = formatted;
    }

    public void setAttributeMarker(final String attributeMarker) {
        this.attributeMarker = attributeMarker;
    }

    public String getAttributeMarker() {
        return attributeMarker;
    }

    @Override
    public void startRecord(final String identifier) {
        if (atStreamStart) {
            if (!omitXmlDeclaration) {
                writeHeader();
                prettyPrintNewLine();
            }
            writeRaw(ROOT_OPEN);
            prettyPrintNewLine();
            incrementIndentationLevel();
        }
        atStreamStart = false;

        prettyPrintIndentation();
        writeRaw(RECORD_OPEN);
        recordAttributeOffset = builder.length() - 1;
        prettyPrintNewLine();

        incrementIndentationLevel();
    }

    @Override
    public void endRecord() {
        decrementIndentationLevel();
        prettyPrintIndentation();
        writeRaw(RECORD_CLOSE);
        prettyPrintNewLine();
        sendAndClearData();
    }

    @Override
    public void startEntity(final String name) {
        currentEntity = name;
        if (!name.equals(Marc21EventNames.LEADER_ENTITY)) {
            if (name.length() != LEADER_ENTITY_LENGTH) {
                final String message = String.format("Entity too short." + "Got a string ('%s') of length %d." +
                        "Expected a length of " + LEADER_ENTITY_LENGTH + " (field + indicators).", name, name.length());
                throw new MetafactureException(message);
            }

            final String tag = name.substring(TAG_BEGIN, TAG_END);
            final String ind1 = name.substring(IND1_BEGIN, IND1_END);
            final String ind2 = name.substring(IND2_BEGIN, IND2_END);
            prettyPrintIndentation();
            writeRaw(String.format(DATAFIELD_OPEN_TEMPLATE, tag, ind1, ind2));
            prettyPrintNewLine();
            incrementIndentationLevel();
        }
    }

    @Override
    public void endEntity() {
        if (!currentEntity.equals(Marc21EventNames.LEADER_ENTITY)) {
            decrementIndentationLevel();
            prettyPrintIndentation();
            writeRaw(DATAFIELD_CLOSE);
            prettyPrintNewLine();
        }
        currentEntity = "";
    }

    @Override
    public void literal(final String name, final String value) {
        if ("".equals(currentEntity)) {
            if (attributeMarker != null && name.startsWith(attributeMarker)) {
                builder.insert(recordAttributeOffset, String.format(ATTRIBUTE_TEMPLATE, name.substring(attributeMarker.length()), value));
            }
            else if (!writeLeader(name, value)) {
                prettyPrintIndentation();
                writeRaw(String.format(CONTROLFIELD_OPEN_TEMPLATE, name));
                if (value != null) {
                    writeEscaped(value.trim());
                }
                writeRaw(CONTROLFIELD_CLOSE);
                prettyPrintNewLine();
            }
        }
        else if (!writeLeader(currentEntity, value)) {
            prettyPrintIndentation();
            writeRaw(String.format(SUBFIELD_OPEN_TEMPLATE, name));
            writeEscaped(value.trim());
            writeRaw(SUBFIELD_CLOSE);
            prettyPrintNewLine();
        }
    }

    @Override
    protected void onResetStream() {
        if (!atStreamStart) {
            writeFooter();
        }
        sendAndClearData();
        atStreamStart = true;
    }

    @Override
    protected void onCloseStream() {
        writeFooter();
        sendAndClearData();
    }

    /** Increments the indentation level by one */
    private void incrementIndentationLevel() {
        indentationLevel += 1;
    }

    /** Decrements the indentation level by one */
    private void decrementIndentationLevel() {
        indentationLevel -= 1;
    }

    /** Adds a XML Header */
    private void writeHeader() {
        writeRaw(String.format(XML_DECLARATION_TEMPLATE, xmlVersion, xmlEncoding));
    }

    /** Closes the root tag */
    private void writeFooter() {
        writeRaw(ROOT_CLOSE);
    }

    /** Writes a unescaped sequence */
    private void writeRaw(final String str) {
        builder.append(str);
    }

    /** Writes a escaped sequence */
    private void writeEscaped(final String str) {
        builder.append(XmlUtil.escape(str, false));
    }

    private boolean writeLeader(final String name, final String value) {
        if (name.equals(Marc21EventNames.LEADER_ENTITY)) {
            prettyPrintIndentation();
            writeRaw(LEADER_OPEN_TEMPLATE + value + LEADER_CLOSE_TEMPLATE);
            prettyPrintNewLine();

            return true;
        }
        else {
            return false;
        }
    }

    private void prettyPrintIndentation() {
        if (formatted) {
            final String prefix = String.join("", Collections.nCopies(indentationLevel, INDENT));
            builder.append(prefix);
        }
    }

    private void prettyPrintNewLine() {
        if (formatted) {
            builder.append(NEW_LINE);
        }
    }

    private void sendAndClearData() {
        getReceiver().process(builder.toString());
        builder.delete(0, builder.length());
        recordAttributeOffset = 0;
    }

}
