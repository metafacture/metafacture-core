/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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

package org.metafacture.json;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Serialises an object as JSON. Records and entities are represented
 * as objects unless their name ends with []. If the name ends with [],
 * an array is created.
 *
 * @author Christoph Böhme
 * @author Michael Büchner
 *
 */
@Description("Serialises an object as JSON")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("encode-json")
public final class JsonEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

    public static final String ARRAY_MARKER = "[]";
    public static final String BOOLEAN_MARKER = null;
    public static final String NUMBER_MARKER = null;

    private static final char ESCAPE_CHAR_LOW = 0x20;
    private static final char ESCAPE_CHAR_HIGH = 0x7f;

    private final JsonGenerator jsonGenerator;
    private final StringWriter writer = new StringWriter();

    private String arrayMarker = ARRAY_MARKER;
    private String booleanMarker = BOOLEAN_MARKER;
    private String numberMarker = NUMBER_MARKER;

    /**
     * Constructs a JsonEncoder if no IOException occurs. The root value
     * separator of the JsonGenerator is set to null.
     */
    public JsonEncoder() {
        try {
            jsonGenerator = new JsonFactory().createGenerator(writer);
            jsonGenerator.setRootValueSeparator(null);
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    /**
     * Sets the array marker.
     *
     * @param arrayMarker the array marker
     */
    public void setArrayMarker(final String arrayMarker) {
        this.arrayMarker = arrayMarker;
    }

    /**
     * Gets the array marker.
     *
     * @return the array marker
     */
    public String getArrayMarker() {
        return arrayMarker;
    }

    /**
     * Sets the boolean marker.
     *
     * @param booleanMarker the boolean marker
     */
    public void setBooleanMarker(final String booleanMarker) {
        this.booleanMarker = booleanMarker;
    }

    /**
     * Gets the boolean marker.
     *
     * @return the boolean marker
     */
    public String getBooleanMarker() {
        return booleanMarker;
    }

    /**
     * Sets the number marker.
     *
     * @param numberMarker the number marker
     */
    public void setNumberMarker(final String numberMarker) {
        this.numberMarker = numberMarker;
    }

    /**
     * Gets the number marker.
     *
     * @return the number marker
     */
    public String getNumberMarker() {
        return numberMarker;
    }

    /**
     * Flags whether to use pretty printing.
     *
     * @param prettyPrinting true if pretty printing should be used
     */
    public void setPrettyPrinting(final boolean prettyPrinting) {
        jsonGenerator.setPrettyPrinter(prettyPrinting ? new DefaultPrettyPrinter((SerializableString) null) : null);
    }

    /**
     * Checks if the {@link JsonGenerator} has a pretty printer.
     *
     * @return true if {@link JsonGenerator} has a pretty printer.
     */
    public boolean getPrettyPrinting() {
        return jsonGenerator.getPrettyPrinter() != null;
    }

    /**
     * By default JSON output does only have escaping where it is strictly
     * necessary. This is recommended in the most cases. Nevertheless it can
     * be sometimes useful to have some more escaping.
     *
     * @param escapeCharacters an array which defines which characters should be
     *                         escaped and how it will be done. See
     *                         {@link CharacterEscapes}. In most cases this should
     *                         be null. Use like this:
     *                         <pre>{@code int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();
     *                            // and force escaping of a few others:
     *                            esc['\''] = CharacterEscapes.ESCAPE_STANDARD;
     *                         JsonEncoder.useEscapeJavaScript(esc);
     *                         }</pre>
     */
    public void setJavaScriptEscapeChars(final int[] escapeCharacters) {

        final CharacterEscapes ce = new CharacterEscapes() {

            private static final long serialVersionUID = 1L;

            @Override
            public int[] getEscapeCodesForAscii() {
                if (escapeCharacters == null) {
                    return CharacterEscapes.standardAsciiEscapesForJSON();
                }
                return escapeCharacters;
            }

            @Override
            public SerializableString getEscapeSequence(final int ch) {
                final String jsEscaped = escapeChar((char) ch);
                return new SerializedString(jsEscaped);
            }

        };

        jsonGenerator.setCharacterEscapes(ce);
    }

    @Override
    public void startRecord(final String id) {
        final StringBuffer buffer = writer.getBuffer();
        buffer.delete(0, buffer.length());
        startGroup(id);
    }

    @Override
    public void endRecord() {
        endGroup();
        try {
            jsonGenerator.flush();
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
        getReceiver().process(writer.toString());
    }

    @Override
    public void startEntity(final String name) {
        startGroup(name);
    }

    @Override
    public void endEntity() {
        endGroup();
    }

    @Override
    public void literal(final String name, final String value) {
        try {
            final JsonStreamContext ctx = jsonGenerator.getOutputContext();
            if (ctx.inObject()) {
                jsonGenerator.writeFieldName(getUnmarkedName(name, booleanMarker, numberMarker));
            }

            if (value == null) {
                jsonGenerator.writeNull();
            }
            else if (isMarkedName(name, booleanMarker)) {
                jsonGenerator.writeBoolean(Boolean.parseBoolean(value));
            }
            else if (isMarkedName(name, numberMarker)) {
                jsonGenerator.writeNumber(value);
            }
            else {
                jsonGenerator.writeString(value);
            }
        }
        catch (final JsonGenerationException e) {
            throw new MetafactureException(e);
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    private void startGroup(final String name) {
        try {
            final JsonStreamContext ctx = jsonGenerator.getOutputContext();
            if (isMarkedName(name, arrayMarker)) {
                if (ctx.inObject()) {
                    jsonGenerator.writeFieldName(getUnmarkedName(name, arrayMarker));
                }
                jsonGenerator.writeStartArray();
            }
            else {
                if (ctx.inObject()) {
                    jsonGenerator.writeFieldName(name);
                }
                jsonGenerator.writeStartObject();
            }
        }
        catch (final JsonGenerationException e) {
            throw new MetafactureException(e);
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    private void endGroup() {
        try {
            final JsonStreamContext ctx = jsonGenerator.getOutputContext();
            if (ctx.inObject()) {
                jsonGenerator.writeEndObject();
            }
            else if (ctx.inArray()) {
                jsonGenerator.writeEndArray();
            }
        }
        catch (final JsonGenerationException e) {
            throw new MetafactureException(e);
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    private boolean isMarkedName(final String name, final String marker) {
        return marker != null && name.endsWith(marker);
    }

    private String getUnmarkedName(final String name, final String... markers) {
        for (final String marker : markers) {
            if (isMarkedName(name, marker)) {
                return name.substring(0, name.length() - marker.length());
            }
        }

        return name;
    }

    private String escapeChar(final char ch) {
        final String namedEscape = namedEscape(ch);
        return namedEscape != null ? namedEscape : (ch < ESCAPE_CHAR_LOW || ESCAPE_CHAR_HIGH < ch) ? unicodeEscape(ch) : Character.toString(ch);
    }

    private String namedEscape(final char ch) {
        final String result;

        switch (ch) {
            case '\b':
                result = "\\b";
                break;
            case '\n':
                result = "\\n";
                break;
            case '\t':
                result = "\\t";
                break;
            case '\f':
                result = "\\f";
                break;
            case '\r':
                result = "\\r";
                break;
            case '\'':
                result = "\\'";
                break;
            case '\\':
                result = "\\\\";
                break;
            case '"':
                result = "\\\"";
                break;
            case '/':
                result = "\\/";
                break;
            default:
                result = null;
        }

        return result;
    }

    private String unicodeEscape(final char ch) {
        return String.format("\\u%4H", ch).replace(' ', '0');
    }

}
