/*
 * Copyright 2020, 2022 Fabian Steeg, hbz
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

package org.metafacture.io;

import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@link SitemapReader}.
 *
 * @author Fabian Steeg
 *
 */
public final class SitemapReaderTest {

    private String sitemap = "sitemap.xml";
    private SitemapReader sitemapReader;

    @Mock
    private ObjectReceiver<String> receiver;
    private InOrder inOrder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        sitemapReader = new SitemapReader();
        sitemapReader.setWait(0); // we're not actually crawling any urls in the tests
        sitemapReader.setReceiver(receiver);
        inOrder = Mockito.inOrder(receiver);
    }

    @Test
    public void testShouldProcessAll() {
        sitemapReader.process(getClass().getResource(sitemap).toString());
        inOrder.verify(receiver).process("https://www.oncampus.de/Customer_Experience_Management");
        inOrder.verify(receiver).process("https://www.oncampus.de/Propädeutik_Mathe_Grundlagen");
        inOrder.verify(receiver).process("https://www.oncampus.de/MDR/Websession2020");
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testShouldProcessPattern() {
        sitemapReader.process(getClass().getResource(sitemap).toString());
        sitemapReader.setFilter(".*/MDR/.*");
        inOrder.verify(receiver).process("https://www.oncampus.de/MDR/Websession2020");
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = MetafactureException.class)
    public void testShouldThrowOnInvalidUrl() {
        sitemapReader.process("");
    }

    @After
    public void cleanup() {
        sitemapReader.closeStream();
    }
}
