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

import org.metafacture.framework.ObjectReceiver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.StringReader;

/**
 * Tests for {@link ElementExtractor}.
 *
 * @author Fabian Steeg
 *
 */
public final class ElementExtractorTest {

    private static final StringReader IN = new StringReader("<html>" +
            "<script data-test='site-head-data'>{\"code\":\"hey\"}</script>" +
            "<script data-test='model-linked-data'>{\"code\":\"yo\"}");

    private static final String OUT = "{\"code\":\"yo\"}";

    @Mock
    private ObjectReceiver<String> receiver;

    private ElementExtractor elementExtractor;

    public ElementExtractorTest() {
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        elementExtractor = new ElementExtractor("script[data-test=model-linked-data]");
        elementExtractor.setReceiver(receiver);
    }

    @Test
    public void testShouldProcessRecordsFollowedbySeparator() {
        elementExtractor.process(IN);
        Mockito.verify(receiver).process(OUT);
        Mockito.verifyNoMoreInteractions(receiver);
    }

    @After
    public void cleanup() {
        elementExtractor.closeStream();
    }
}
