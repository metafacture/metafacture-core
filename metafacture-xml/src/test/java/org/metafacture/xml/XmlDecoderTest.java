/*
 * Copyright 2024 Pascal Christoph (hbz)
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

package org.metafacture.xml;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.MetafactureException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Tests for class {@link XmlDecoder}.
 *
 * @author Pascal Christoph (dr0i)
 */
public final class XmlDecoderTest {

    private final String TEST_XML_WITH_TWO_ENTITIES = "<record>&gt;&gt;</record>";
    private XmlDecoder xmlDecoder;
    private final Reader reader = new StringReader(TEST_XML_WITH_TWO_ENTITIES);

    @Before
    public void initSystemUnderTest() {
        xmlDecoder = new XmlDecoder();
    }

    @Test
    public void issue554_default() {
        process(xmlDecoder);
    }

    @Test(expected = MetafactureException.class)
    public void issue554_shouldFail() {
        xmlDecoder.setTotalEntitySizeLimit("1");
        process(xmlDecoder);
    }

    @Test
    public void issue554_unlimitedEntities() {
        xmlDecoder.setTotalEntitySizeLimit("0");
        process(xmlDecoder);
    }

    private void process(XmlDecoder xmlDecoder) {
        try {
            xmlDecoder.process(reader);
            reader.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
