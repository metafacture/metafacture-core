/*
 * Copyright 2016 Christoph Böhme
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

package org.metafacture.metamorph;

import org.metafacture.framework.helpers.DefaultStreamReceiver;
import org.metafacture.metamorph.api.Maps;
import org.metafacture.metamorph.api.NamedValueReceiver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Tests for class {@link Metamorph}.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme (rewrite)
 */
public final class MetamorphTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private NamedValueReceiver receiver;

    private Metamorph metamorph;

    public MetamorphTest() {
    }

    @Before
    public void createSystemUnderTest() {
        metamorph = new Metamorph();
        metamorph.setReceiver(new DefaultStreamReceiver());
    }

    @Test
    public void shouldMapMatchingPath() {
        assertNamedValue(true, i -> {
            i.literal("testEntity.testLiteral", "testValue");
        });
    }

    @Test
    public void shouldNotMapNonMatchingPath() {
        assertNamedValue(false, i -> {
            i.literal("nonMatching.path", "testValue");
        });
    }

    @Test
    public void shouldMapMatchingLiteralInMatchingEntity() {
        assertNamedValue(true, i -> {
            i.startEntity("testEntity");
            i.literal("testLiteral", "testValue");
        });
    }

    @Test
    public void shouldNotMapNonMatchingLiteralInMatchingEntity() {
        assertNamedValue(false, i -> {
            i.startEntity("testEntity");
            i.literal("nonMatching", "testValue");
        });
    }

    @Test
    public void shouldNotMapMatchingLiteralInNonMatchingEntity() {
        assertNamedValue(false, i -> {
            i.startEntity("nonMatching");
            i.literal("testLiteral", "testValue");
        });
    }

    @Test
    public void shouldNotMapLiteralWithoutMatchingEntity() {
        assertNamedValue(false, i -> {
            i.literal("testLiteral", "testValue");
        });
    }

    @Test
    public void shouldFedbackLiteralsStartingWithAtIntoMetamorph() {
        assertNamedValue(true, i -> {
            final Data data1;
            data1 = new Data();
            data1.setName("@feedback");
            i.addNamedValueSource(data1);
            i.registerNamedValueReceiver("testLiteral", data1);

            final Data data2 = new Data();
            data2.setName("outName");
            data2.setNamedValueReceiver(receiver);
            i.registerNamedValueReceiver("@feedback", data2);

            i.literal("testLiteral", "testValue");
        });
    }

    @Test
    public void shouldReturnValueFromNestedMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("outName", "testValue");

        metamorph.putMap("testMap", map);

        Assert.assertNotNull(metamorph.getMap("testMap"));
        Assert.assertEquals("testValue", metamorph.getValue("testMap", "outName"));
    }

    @Test
    public void shouldReturnDefaultValueIfMapIsKnownButNameIsUnknown() {
        final Map<String, String> map = new HashMap<>();
        map.put(Maps.DEFAULT_MAP_KEY, "defaultValue");

        metamorph.putMap("testMap", map);

        Assert.assertEquals("defaultValue", metamorph.getValue("testMap", "nameNotInMap"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionIfEntityIsNotClosed() {
        metamorph.startRecord("");
        metamorph.startEntity("testEntity");
        metamorph.startEntity("testEntity");
        metamorph.endEntity();
        metamorph.endRecord();  // Exception expected
    }

    private void assertNamedValue(final boolean matching, final Consumer<Metamorph> in) {
        /**
         * Creates the Metamorph structure that corresponds to the Metamorph XML
         * statement {@code <data source="testEntity.testLiteral" name="outName" />}.
         */
        final Data data = new Data();
        data.setName("outName");
        data.setNamedValueReceiver(receiver);
        metamorph.registerNamedValueReceiver("testEntity" + '.' + "testLiteral", data);

        metamorph.startRecord("");
        in.accept(metamorph);

        try {
            if (matching) {
                Mockito.verify(receiver).receive(
                        ArgumentMatchers.eq("outName"), ArgumentMatchers.eq("testValue"),
                        ArgumentMatchers.any(), ArgumentMatchers.eq(1), ArgumentMatchers.anyInt());
            }

            Mockito.verifyNoMoreInteractions(receiver);
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

}
