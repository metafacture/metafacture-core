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

package org.metafacture.yaml;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Serialises an object as YAML. Records and entities are represented
 * as objects unless their name ends with []. If the name ends with [],
 * an array is created.
 *
 * @author Christoph Böhme
 * @author Michael Büchner
 * @author Jens Wille
 *
 */
@Description("Serialises an object as YAML")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("encode-yaml")
public final class YamlEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {

    public static final String ARRAY_MARKER = "[]";

    private static final char ESCAPE_CHAR_LOW = 0x20;
    private static final char ESCAPE_CHAR_HIGH = 0x7f;

    private final YAMLGenerator yamlGenerator;
    private final StringWriter writer = new StringWriter();

    private String arrayMarker = ARRAY_MARKER;

    /**
     * Creates an instance of {@link YamlEncoder} if no IOException occurs.
     */
    public YamlEncoder() {
        try {
            yamlGenerator = new YAMLFactory().createGenerator(writer);
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
     * By default YAML output does only have escaping where it is strictly
     * necessary. This is recommended in the most cases. Nevertheless it can
     * be sometimes useful to have some more escaping.
     *
     * @param escapeCharacters an array which defines which characters should be
     *                         escaped and how it will be done. See
     *                         {@link CharacterEscapes}. In most cases this should
     *                         be null. Use like this:
     *                         <pre>{@code int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();
     *                         // and force escaping of a few others:
     *                         esc['\''] = CharacterEscapes.ESCAPE_STANDARD;
     *                         yamlEncoder.setJavaScriptEscapeChars(esc);
     *                         }</pre>
     */
    public void setJavaScriptEscapeChars(final int[] escapeCharacters) {
        final CharacterEscapes ce = new CharacterEscapes() {

            private static final long serialVersionUID = 1L;

            @Override
            public int[] getEscapeCodesForAscii() {
                return escapeCharacters != null ? escapeCharacters : CharacterEscapes.standardAsciiEscapesForJSON();
            }

            @Override
            public SerializableString getEscapeSequence(final int ch) {
                return new SerializedString(escapeChar((char) ch));
            }

        };

        yamlGenerator.setCharacterEscapes(ce);
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
            yamlGenerator.flush();
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
            final JsonStreamContext ctx = yamlGenerator.getOutputContext();

            if (ctx.inObject()) {
                yamlGenerator.writeFieldName(name);
            }

            if (value == null) {
                yamlGenerator.writeNull();
            }
            else {
                yamlGenerator.writeString(value);
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
            final JsonStreamContext ctx = yamlGenerator.getOutputContext();

            if (name.endsWith(arrayMarker)) {
                if (ctx.inObject()) {
                    yamlGenerator.writeFieldName(name.substring(0, name.length() - arrayMarker.length()));
                }

                yamlGenerator.writeStartArray();
            }
            else {
                if (ctx.inObject()) {
                    yamlGenerator.writeFieldName(name);
                }

                yamlGenerator.writeStartObject();
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
            final JsonStreamContext ctx = yamlGenerator.getOutputContext();

            if (ctx.inObject()) {
                yamlGenerator.writeEndObject();
            }
            else if (ctx.inArray()) {
                yamlGenerator.writeEndArray();
            }
        }
        catch (final JsonGenerationException e) {
            throw new MetafactureException(e);
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
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
