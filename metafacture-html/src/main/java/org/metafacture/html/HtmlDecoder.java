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

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Decode HTML to metadata events. Each input document represents one record.
 * 
 * @author Fabian Steeg (fsteeg)
 *
 */
@Description("Decode HTML to metadata events. The attrValsAsSubfields option can be used to override "
        + "the default attribute values to be used as subfields (e.g. by default "
        + "`link rel=\"canonical\" href=\"http://example.org\"` becomes `link.canonical`). "
        + "It expects an HTTP-style query string specifying as key the attributes whose value should "
        + "be used as a subfield, and as value the attribute whose value should be the subfield value, "
        + "e.g. the default contains `link.rel=href`. To use the HTML element text as the value "
        + "(instead of another attribute), omit the value of the query-string key-value pair, "
        + "e.g. `title.lang`. To add to the defaults, instead of replacing them, start with an `&`, "
        + "e.g. `&h3.class`")
@In(Reader.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-html")
public class HtmlDecoder extends DefaultObjectPipe<Reader, StreamReceiver> {

    private static final String DEFAULT_ATTR_VALS_AS_SUBFIELDS = //
            "meta.name=content&meta.property=content&link.rel=href&a.rel=href";
    private Map<String, String> attrValsAsSubfields;

    public HtmlDecoder() {
        setAttrValsAsSubfields(DEFAULT_ATTR_VALS_AS_SUBFIELDS);
    }

    @Override
    public void process(final Reader reader) {
        try {
            StreamReceiver receiver = getReceiver();
            receiver.startRecord(UUID.randomUUID().toString());
            Document document = Jsoup.parse(IOUtils.toString(reader));
            process(document, receiver);
            receiver.endRecord();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(Element parent, StreamReceiver receiver) {
        for (Element element : parent.children()) {
            receiver.startEntity(element.nodeName());
            Attributes attributes = element.attributes();
            for (Attribute attribute : attributes) {
                handleAttributeValuesAsSubfields(receiver, element, attributes, attribute);
                receiver.literal(attribute.getKey(), attribute.getValue());
            }
            if (element.children().isEmpty()) {
                String text = element.text().trim();
                String value = text.isEmpty() ? element.data() : text;
                if (!value.isEmpty()) {
                    receiver.literal("value", value);
                }
            }
            process(element, receiver);
            receiver.endEntity();
        }
    }

    private void handleAttributeValuesAsSubfields(StreamReceiver receiver, Element element,
            Attributes attributes, Attribute attribute) {
        String fullFieldKey = element.nodeName() + "." + attribute.getKey();
        if (attrValsAsSubfields.containsKey(fullFieldKey)) {
            String configValue = attrValsAsSubfields.get(fullFieldKey);
            if (configValue.trim().isEmpty()) {
                receiver.literal(attribute.getValue(), element.text().trim());
            } else {
                String value = attributes.get(configValue);
                receiver.literal(attribute.getValue(), value);
            }
        }
    }

    public void setAttrValsAsSubfields(String mapString) {
        this.attrValsAsSubfields = new HashMap<String, String>();
        String input = mapString.startsWith("&") ? DEFAULT_ATTR_VALS_AS_SUBFIELDS + mapString
                : mapString;
        for (String nameValuePair : input.split("&")) {
            String[] nameValue = nameValuePair.split("=");
            try {
                String utf8 = StandardCharsets.UTF_8.name();
                String key = URLDecoder.decode(nameValue[0], utf8);
                String val = nameValue.length > 1 ? URLDecoder.decode(nameValue[1], utf8) : "";
                attrValsAsSubfields.put(key, val);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
