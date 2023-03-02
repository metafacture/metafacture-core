/*
 * Copyright 2020, 2021 Fabian Steeg, hbz
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

package org.metafacture.html;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Decode HTML to metadata events. Each input document represents one record.
 *
 * @author Fabian Steeg (fsteeg)
 *
 */
@Description("Decode HTML to metadata events. The attrValsAsSubfields option can be used to override " +
        "the default attribute values to be used as subfields (e.g. by default " +
        "`link rel=\"canonical\" href=\"http://example.org\"` becomes `link.canonical`). " +
        "It expects an HTTP-style query string specifying as key the attributes whose value should " +
        "be used as a subfield, and as value the attribute whose value should be the subfield value, " +
        "e.g. the default contains `link.rel=href`. To use the HTML element text as the value " +
        "(instead of another attribute), omit the value of the query-string key-value pair, " +
        "e.g. `title.lang`. To add to the defaults, instead of replacing them, start with an `&`, " +
        "e.g. `&h3.class`")
@In(Reader.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-html")
public class HtmlDecoder extends DefaultObjectPipe<Reader, StreamReceiver> {

    private static final Logger LOG = LoggerFactory.getLogger(HtmlDecoder.class);

    private static final String DEFAULT_ATTR_VALS_AS_SUBFIELDS = //
            "meta.name=content&meta.property=content&link.rel=href&a.rel=href";

    private Map<String, String> attrValsAsSubfields;

    /**
     * Creates an instance of {@link HtmlDecoder}.
     */
    public HtmlDecoder() {
        setAttrValsAsSubfields(DEFAULT_ATTR_VALS_AS_SUBFIELDS);
    }

    @Override
    public void process(final Reader reader) {
        try {
            final StreamReceiver receiver = getReceiver();
            receiver.startRecord(UUID.randomUUID().toString());
            final Document document = Jsoup.parse(IOUtils.toString(reader));
            process(document, receiver);
            receiver.endRecord();
        }
        catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void process(final Element parent, final StreamReceiver receiver) {
        for (final Element element : parent.children()) {
            receiver.startEntity(element.nodeName());
            final Attributes attributes = element.attributes();
            boolean addedValueAsSubfield = false;
            for (final Attribute attribute : attributes) {
                addedValueAsSubfield = handleAttributeValuesAsSubfields(receiver, element, attributes, attribute);
                receiver.literal(attribute.getKey(), attribute.getValue());
            }
            final String text = element.text().trim();
            final String value = text.isEmpty() ? element.data() : text;
            if (!value.isEmpty() && !addedValueAsSubfield) {
                receiver.literal("value", value);
            }
            process(element, receiver);
            receiver.endEntity();
        }
    }

    private boolean handleAttributeValuesAsSubfields(final StreamReceiver receiver, final Element element, final Attributes attributes, final Attribute attribute) {
        final String fullFieldKey = element.nodeName() + "." + attribute.getKey();
        if (attrValsAsSubfields.containsKey(fullFieldKey)) {
            final String configValue = attrValsAsSubfields.get(fullFieldKey);
            if (configValue.trim().isEmpty()) {
                receiver.literal(attribute.getValue(), element.text().trim());
                return true;
            }
            else {
                final String value = attributes.get(configValue);
                receiver.literal(attribute.getValue(), value);
            }
        }
        return false;
    }

    /**
     * Sets attribute values as subfields. If the value(s) start with an `&amp;` they
     * are appended to {@link #DEFAULT_ATTR_VALS_AS_SUBFIELDS}.
     *
     * @param mapString the attributes to be added as subfields
     */
    public void setAttrValsAsSubfields(final String mapString) {
        this.attrValsAsSubfields = new HashMap<String, String>();
        final String input = mapString.startsWith("&") ? DEFAULT_ATTR_VALS_AS_SUBFIELDS + mapString : mapString;
        for (final String nameValuePair : input.split("&")) {
            final String[] nameValue = nameValuePair.split("=");
            try {
                final String utf8 = StandardCharsets.UTF_8.name();
                final String key = URLDecoder.decode(nameValue[0], utf8);
                final String val = nameValue.length > 1 ? URLDecoder.decode(nameValue[1], utf8) : "";
                attrValsAsSubfields.put(key, val);
            }
            catch (final UnsupportedEncodingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

}
