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

import java.util.Collections;

import org.metafacture.commons.XmlUtil;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

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

    private final StringBuilder builder;

    private boolean atStreamStart;

    private boolean omitXmlDeclaration;
    private String xmlVersion;
    private String xmlEncoding;

    private String currentEntity;
    private int indentationLevel;
    private boolean formatted;

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

    public void omitXmlDeclaration(boolean omitXmlDeclaration) {
        this.omitXmlDeclaration = omitXmlDeclaration;
    }

    public void setXmlVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public void setXmlEncoding(String xmlEncoding) {
        this.xmlEncoding = xmlEncoding;
    }

    /**
     * Formats the resulting xml, by indentation.
     * 
     * @param formatted True, if formatting is activated.
     */
    public void setFormatted(boolean formatted) {
        this.formatted = formatted;
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
            if (name.length() != 5) {
                String message = String.format("Entity too short." + "Got a string ('%s') of length %d."
                        + "Expected a length of 5 (field + indicators).", name, name.length());
                throw new MetafactureException(message);
            }

            String tag = name.substring(0, 3);
            String ind1 = name.substring(3, 4);
            String ind2 = name.substring(4, 5);
            prettyPrintIndentation();
            writeRaw(String.format(DATAFIELD_OPEN_TEMPLATE, tag, ind1, ind2));
            prettyPrintNewLine();
            incrementIndentationLevel();
        } else {
            prettyPrintIndentation();
            writeRaw(LEADER_OPEN_TEMPLATE);
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
        if (value == null || value.isEmpty())
            return;
        if (currentEntity.equals("")) {
            prettyPrintIndentation();
            writeRaw(String.format(CONTROLFIELD_OPEN_TEMPLATE, name.replaceFirst("\\W", "")));
            writeEscaped(value.trim());
            writeRaw(CONTROLFIELD_CLOSE);
            prettyPrintNewLine();
        } else if (!currentEntity.equals(Marc21EventNames.LEADER_ENTITY)) {
            prettyPrintIndentation();
            writeRaw(String.format(SUBFIELD_OPEN_TEMPLATE, name.charAt(name.indexOf('.') + 1)));
            writeEscaped(value.trim());
            writeRaw(SUBFIELD_CLOSE);
            prettyPrintNewLine();
        } else {
            {
                writeRaw(value + LEADER_CLOSE_TEMPLATE);
                prettyPrintNewLine();
            }
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

    private void prettyPrintIndentation() {
        if (formatted) {
            String prefix = String.join("", Collections.nCopies(indentationLevel, INDENT));
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
    }
}
