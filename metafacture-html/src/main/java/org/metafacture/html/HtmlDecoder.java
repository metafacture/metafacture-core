/*
 * Copyright 2020 Fabian Steeg, hbz
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
@Description("Decode HTML to metadata events")
@In(Reader.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-html")
public class HtmlDecoder extends DefaultObjectPipe<Reader, StreamReceiver> {

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
}
