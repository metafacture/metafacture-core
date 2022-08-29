/*
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

package org.metafacture.yaml;

import org.metafacture.framework.ObjectReceiver;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Tests for class {@link YamlEncoder}.
 *
 * @author Christoph Böhme
 *
 */
public final class YamlEncoderTest {

    private static final String LITERAL1 = "L1";
    private static final String LITERAL2 = "L2";
    private static final String LITERAL3 = "L3";
    private static final String LITERAL4 = "L4";

    private static final String VALUE1 = "V1";
    private static final String VALUE2 = "V2";
    private static final String VALUE3 = "V3";
    private static final String VALUE4 = "V4";

    private static final String ENTITY1 = "En1";
    private static final String ENTITY2 = "En2";

    private static final String LIST1 = "Li1[]";
    private static final String LIST2 = "Li2[]";
    private static final String LIST3 = "Li3[]";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ObjectReceiver<String> receiver;

    @Test
    public void testShouldEncodeLiterals() {
        assertEncode(
                i -> {
                    i.startRecord("");
                    i.literal(LITERAL1, VALUE1);
                    i.literal(LITERAL2, VALUE2);
                    i.literal(LITERAL3, VALUE3);
                    i.endRecord();
                },
                "---\n" +
                "L1: 'V1'\n" +
                "L2: 'V2'\n" +
                "L3: 'V3'"
        );
    }

    @Test
    public void testShouldEncodeEntities() {
        assertEncode(
                i -> {
                    i.startRecord("");
                    i.startEntity(ENTITY1);
                    i.literal(LITERAL1, VALUE1);
                    i.literal(LITERAL2, VALUE2);
                    i.endEntity();
                    i.startEntity(ENTITY2);
                    i.literal(LITERAL1, VALUE1);
                    i.literal(LITERAL2, VALUE2);
                    i.endEntity();
                    i.endRecord();
                },
                "---\n" +
                "En1:\n" +
                "  L1: 'V1'\n" +
                "  L2: 'V2'\n" +
                "En2:\n" +
                "  L1: 'V1'\n" +
                "  L2: 'V2'"
        );
    }

    @Test
    public void testShouldEncodeNestedEntities() {
        assertEncode(
                i -> {
                    i.startRecord("");
                    i.startEntity(ENTITY1);
                    i.startEntity(ENTITY2);
                    i.literal(LITERAL1, VALUE1);
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                "---\n" +
                "En1:\n" +
                "  En2:\n" +
                "    L1: 'V1'"
        );
    }

    @Test
    public void testShouldEncodeMarkedEntitiesAsList() {
        assertEncode(
                i -> {
                    i.startRecord("");
                    i.startEntity(LIST1);
                    i.literal(LITERAL1, VALUE1);
                    i.literal(LITERAL2, VALUE2);
                    i.literal(LITERAL3, VALUE3);
                    i.endEntity();
                    i.endRecord();
                },
                "---\n" +
                "Li1:\n" +
                "- 'V1'\n" +
                "- 'V2'\n" +
                "- 'V3'"
        );
    }

    @Test
    public void testShouldEncodeEntitiesInLists() {
        assertEncode(
                i -> {
                    i.startRecord("");
                    i.startEntity(LIST1);
                    i.startEntity(ENTITY1);
                    i.literal(LITERAL1, VALUE1);
                    i.literal(LITERAL2, VALUE2);
                    i.endEntity();
                    i.startEntity(ENTITY2);
                    i.literal(LITERAL3, VALUE3);
                    i.literal(LITERAL4, VALUE4);
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                "---\n" +
                "Li1:\n" +
                "- L1: 'V1'\n" +
                "  L2: 'V2'\n" +
                "- L3: 'V3'\n" +
                "  L4: 'V4'"
        );
    }

    @Test
    public void testShouldEncodeNestedLists() {
        assertEncode(
                i -> {
                    i.startRecord("");
                    i.startEntity(LIST1);
                    i.startEntity(LIST2);
                    i.literal(LITERAL1, VALUE1);
                    i.literal(LITERAL2, VALUE2);
                    i.endEntity();
                    i.startEntity(LIST3);
                    i.literal(LITERAL3, VALUE3);
                    i.literal(LITERAL4, VALUE4);
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                "---\n" +
                "Li1:\n" +
                "- - 'V1'\n" +
                "  - 'V2'\n" +
                "- - 'V3'\n" +
                "  - 'V4'"
        );
    }

    @Test
    public void testShouldNotEncodeEntitiesWithDifferentMarkerAsList() {
        assertEncode(
                i -> {
                    i.setArrayMarker("*");

                    i.startRecord("");
                    i.startEntity(LIST1);
                    i.literal(LITERAL1, VALUE1);
                    i.literal(LITERAL2, VALUE2);
                    i.literal(LITERAL3, VALUE3);
                    i.endEntity();
                    i.endRecord();
                },
                "---\n" +
                "Li1[]:\n" +
                "  L1: 'V1'\n" +
                "  L2: 'V2'\n" +
                "  L3: 'V3'"
        );
    }

    @Test
    public void testShouldEncodeMarkedEntitiesWithConfiguredMarkerAsList() {
        assertEncode(
                i -> {
                    final String marker = "*";
                    i.setArrayMarker(marker);

                    i.startRecord("");
                    i.startEntity(LIST1.replace(YamlEncoder.ARRAY_MARKER, marker));
                    i.literal(LITERAL1, VALUE1);
                    i.literal(LITERAL2, VALUE2);
                    i.literal(LITERAL3, VALUE3);
                    i.endEntity();
                    i.endRecord();
                },
                "---\n" +
                "Li1:\n" +
                "- 'V1'\n" +
                "- 'V2'\n" +
                "- 'V3'"
        );
    }

    @Test
    public void testShouldOutputDuplicateNames() {
        assertEncode(
                i -> {
                    i.startRecord("");
                    i.literal(LITERAL1, VALUE1);
                    i.literal(LITERAL1, VALUE2);
                    i.endRecord();
                },
                "---\n" +
                "L1: 'V1'\n" +
                "L1: 'V2'"
        );
    }

    @Test
    public void testShouldPrefixOutputWithNewline() {
        assertEncode(
                i -> {
                    i.startRecord("");
                    i.literal(LITERAL1, VALUE1);
                    i.endRecord();
                    i.startRecord("");
                    i.literal(LITERAL2, VALUE2);
                    i.endRecord();
                },
                "---\n" +
                "L1: 'V1'",
                "\n" +
                "---\n" +
                "L2: 'V2'"
        );
    }

    private void assertEncode(final Consumer<YamlEncoder> in, final String... out) {
        final InOrder ordered = Mockito.inOrder(receiver);

        final YamlEncoder yamlEncoder = new YamlEncoder();
        yamlEncoder.setReceiver(receiver);
        in.accept(yamlEncoder);

        try {
            Arrays.stream(out).forEach(s -> ordered.verify(receiver).process(useSystemSpecificLineSeparator(fixQuotes(s))));

            ordered.verifyNoMoreInteractions();
            Mockito.verifyNoMoreInteractions(receiver);
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

    /*
     * Utility method which replaces all single quotes in a string with double quotes.
     * This allows to specify the YAML output in the test cases without having to wrap
     * each bit of text in escaped double quotes.
     */
    private String fixQuotes(final String str) {
        return str.replace('\'', '"');
    }

    /*
     * Utility method which replaces the new-line character with the platform-specific
     * line separator. This is necessary since Jackson uses different line separators
     * depending on the system environment.
     */
    private String useSystemSpecificLineSeparator(final String str) {
        return str.replace("\n", System.lineSeparator());
    }

}
