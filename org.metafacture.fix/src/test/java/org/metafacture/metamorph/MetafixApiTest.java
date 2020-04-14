/*
 * Copyright 2013, 2019 Deutsche Nationalbibliothek and others
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

package org.metafacture.metamorph;

import org.metafacture.framework.helpers.DefaultStreamReceiver;
import org.metafacture.metamorph.api.Maps;
import org.metafacture.metamorph.api.NamedValueReceiver;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests the basic functionality of Metafix via API.
 *
 * @author Markus Michael Geipel (MetamorphTest)
 * @author Christoph Böhme (rewrite MetamorphTest)
 * @author Fabian Steeg (MetafixApiTest)
 */
@ExtendWith(MockitoExtension.class)
public class MetafixApiTest {

    private static final String OUT_NAME = "outName";

    private static final String TEST_ENTITY = "testEntity";
    private static final String TEST_LITERAL = "testLiteral";
    private static final String TEST_MAP = "testMap";
    private static final String TEST_VALUE = "testValue";

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private NamedValueReceiver namedValueReceiver;

    private Metafix metafix;

    public MetafixApiTest() {
    }

    @BeforeEach
    public void createSystemUnderTest() {
        metafix = new Metafix();
        metafix.setReceiver(new DefaultStreamReceiver());
    }

    @Test
    public void shouldMapMatchingPath() {
        setupSimpleMappingMorph();

        metafix.startRecord("");
        metafix.literal(TEST_ENTITY + "." + TEST_LITERAL, TEST_VALUE);

        receive();
    }

    @Test
    public void shouldNotMapNonMatchingPath() {
        setupSimpleMappingMorph();

        metafix.startRecord("");
        metafix.literal("nonMatching.path", TEST_VALUE);

        neverReceive();
    }

    @Test
    public void shouldMapMatchingLiteralInMatchingEntity() {
        setupSimpleMappingMorph();

        metafix.startRecord("");
        metafix.startEntity(TEST_ENTITY);
        metafix.literal(TEST_LITERAL, TEST_VALUE);

        receive();
    }

    @Test
    public void shouldNotMapNonMatchingLiteralInMatchingEntity() {
        setupSimpleMappingMorph();

        metafix.startRecord("");
        metafix.startEntity(TEST_ENTITY);
        metafix.literal("nonMatching", TEST_VALUE);

        neverReceive();
    }

    @Test
    public void shouldNotMapMatchingLiteralInNonMatchingEntity() {
        setupSimpleMappingMorph();

        metafix.startRecord("");
        metafix.startEntity("nonMatching");
        metafix.literal(TEST_LITERAL, TEST_VALUE);

        neverReceive();
    }

    @Test
    public void shouldNotMapLiteralWithoutMatchingEntity() {
        setupSimpleMappingMorph();

        metafix.startRecord("");
        metafix.literal(TEST_LITERAL, TEST_VALUE);

        neverReceive();
    }

    @Test
    public void shouldReturnValueFromNestedMap() {
        final Map<String, String> map = new HashMap<>();
        map.put(OUT_NAME, TEST_VALUE);

        metafix.putMap(TEST_MAP, map);

        Assert.assertNotNull(metafix.getMap(TEST_MAP));
        Assert.assertEquals(TEST_VALUE, metafix.getValue(TEST_MAP, OUT_NAME));
    }

    @Test
    public void shouldReturnDefaultValueIfMapIsKnownButNameIsUnknown() {
        final Map<String, String> map = new HashMap<>();
        map.put(Maps.DEFAULT_MAP_KEY, "defaultValue");

        metafix.putMap(TEST_MAP, map);

        Assert.assertEquals("defaultValue", metafix.getValue(TEST_MAP, "nameNotInMap"));
    }

    @Test
    public void shouldFeedbackLiteralsStartingWithAtIntoMetamorph() {
        final Data dataIn;
        dataIn = new Data();
        dataIn.setName("@feedback");
        metafix.addNamedValueSource(dataIn);
        metafix.registerNamedValueReceiver(TEST_LITERAL, dataIn);

        final Data dataOut = new Data();
        dataOut.setName(OUT_NAME);
        dataOut.setNamedValueReceiver(namedValueReceiver);
        metafix.registerNamedValueReceiver("@feedback", dataOut);

        metafix.startRecord("");
        metafix.literal(TEST_LITERAL, TEST_VALUE);

        receive();
    }

    @Test
    public void shouldThrowIllegalStateExceptionIfEntityIsNotClosed() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            metafix.startRecord("");
            metafix.startEntity(TEST_ENTITY);
            metafix.startEntity(TEST_ENTITY);
            metafix.endEntity();
            metafix.endRecord(); // Exception expected
        });
    }

    /**
     * Creates the Metafix object structure that corresponds to the Metafix DSL
     * statement {@code map(testEntity.testLiteral, outName)}.
     */
    private void setupSimpleMappingMorph() {
        final Data data = new Data();
        data.setName(OUT_NAME);
        data.setNamedValueReceiver(namedValueReceiver);
        metafix.registerNamedValueReceiver(TEST_ENTITY + '.' + TEST_LITERAL, data);
    }

    private void receive() {
        Mockito.verify(namedValueReceiver).receive(
                ArgumentMatchers.eq(OUT_NAME),
                ArgumentMatchers.eq(TEST_VALUE),
                ArgumentMatchers.isA(Data.class),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
        );

        Mockito.verifyNoMoreInteractions(namedValueReceiver);
    }

    private void neverReceive() {
        Mockito.verifyZeroInteractions(namedValueReceiver);
    }

}
