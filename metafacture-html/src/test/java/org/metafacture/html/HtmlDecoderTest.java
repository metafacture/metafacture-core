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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link HtmlDecoder}.
 *
 * @author Fabian Steeg
 *
 */
public final class HtmlDecoderTest {

    @Mock
    private StreamReceiver receiver;

    private HtmlDecoder htmlDecoder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        htmlDecoder = new HtmlDecoder();
        htmlDecoder.setReceiver(receiver);
    }

    @Test
    public void htmlElementsAsEntities() {
        htmlDecoder.process(new StringReader("<h1>Header</h1><p>Paragraph</p>"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("html");
        ordered.verify(receiver).startEntity("head");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).startEntity("body");
        ordered.verify(receiver).startEntity("h1");
        ordered.verify(receiver).literal("value", "Header");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).startEntity("p");
        ordered.verify(receiver).literal("value", "Paragraph");
        ordered.verify(receiver, times(3)).endEntity();
    }

    @Test
    public void nestedEntities() {
        htmlDecoder.process(new StringReader("<ul><li>Item</li></ul>"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("ul");
        ordered.verify(receiver).startEntity("li");
        ordered.verify(receiver).literal("value", "Item");
        // elements above plus body, html
        ordered.verify(receiver, times(4)).endEntity();

    }

    @Test
    public void htmlAttributesAsLiterals() {
        htmlDecoder.process(new StringReader("<p class=lead>Text"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("p");
        ordered.verify(receiver).literal("class", "lead");
        ordered.verify(receiver).literal("value", "Text");
        // elements above plus body, html
        ordered.verify(receiver, times(3)).endEntity();
    }

    @Test
    public void htmlScriptElementData() {
        htmlDecoder.process(new StringReader("<script type=application/ld+json>{\"id\":\"theId\"}</script>"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("script");
        ordered.verify(receiver).literal("type", "application/ld+json");
        ordered.verify(receiver).literal("value", "{\"id\":\"theId\"}");
        // elements above plus body, html
        ordered.verify(receiver, times(4)).endEntity();
    }

    @Test
    public void htmlAttributesAsSubfieldsDefault() {
        htmlDecoder.process(new StringReader("<meta name=\"language\" content=\"DE\"/>"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("meta");
        ordered.verify(receiver).literal("language", "DE");
        ordered.verify(receiver, times(4)).endEntity();
    }
    
    @Test
    public void htmlAttributesAsSubfieldsCustom() {
        htmlDecoder.setAttrValsAsSubfields("mods:url.access");
        htmlDecoder.process(new StringReader("<mods:url access=\"preview\">file:///img.png</mods:url>"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("mods:url");
        ordered.verify(receiver).literal("preview", "file:///img.png");
        ordered.verify(receiver, times(3)).endEntity();
    }

    @Test
    public void htmlAttributesAsSubfieldsDefaultPlusCustom() {
        htmlDecoder.setAttrValsAsSubfields("&mods:url.access");
        htmlDecoder.process(new StringReader("<meta name=\"language\" content=\"DE\"/>"
                + "<mods:url access=\"preview\">file:///img.png</mods:url>"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("meta");
        ordered.verify(receiver).literal("language", "DE");
        ordered.verify(receiver).startEntity("mods:url");
        ordered.verify(receiver).literal("preview", "file:///img.png");
        ordered.verify(receiver, times(3)).endEntity();
    }
}
