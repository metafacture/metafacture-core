/*
 * Copyright 2022 hbz NRW
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

package org.metafacture.metafix;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

// TODO: Utilize JsonDecoder/JsonEncoder instead?

@FunctionalInterface
public interface JsonValue {

    /**
     * Generates the JSON serialization.
     *
     * @param jsonGenerator the JSON generator
     */
    void toJson(JsonGenerator jsonGenerator);

    /**
     * Converts the value to JSON without pretty printing.
     *
     * @return the JSON serialization
     *
     * @throws IOException if an I/O error occurs
     */
    default String toJson() throws IOException {
        return toJson(false);
    }

    /**
     * Converts the value to JSON with optional pretty printing.
     *
     * @param prettyPrinting true if pretty printing should be used
     *
     * @return the JSON serialization
     *
     * @throws IOException if an I/O error occurs
     */
    default String toJson(final boolean prettyPrinting) throws IOException {
        final StringWriter writer = new StringWriter();
        final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(writer);
        jsonGenerator.setPrettyPrinter(prettyPrinting ? new DefaultPrettyPrinter((SerializableString) null) : null);

        try {
            toJson(jsonGenerator);
        }
        catch (final UncheckedIOException e) {
            throw e.getCause();
        }

        jsonGenerator.flush();

        return writer.toString();
    }

    class Parser {

        private static final ObjectMapper MAPPER = new ObjectMapper();

        /**
         * Creates an instance of {@link Parser}.
         */
        public Parser() {
        }

        /**
         * Parses the JSON serialization.
         *
         * @param source the JSON serialization
         *
         * @return the deserialized value
         *
         * @throws IOException if an I/O error occurs
         */
        public Value parse(final String source) throws IOException {
            return parse(MAPPER.readTree(source));
        }

        private Value parse(final JsonNode node) {
            final Value value;

            if (node.isObject()) {
                value = Value.newHash(h -> node.fields().forEachRemaining(e -> h.put(e.getKey(), parse(e.getValue()))));
            }
            else if (node.isArray()) {
                value = Value.newArray(a -> node.elements().forEachRemaining(v -> a.add(parse(v))));
            }
            else {
                value = new Value(node.textValue());
            }

            return value;
        }

    }

}
