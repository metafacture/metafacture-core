/*
 * Copyright 2017 hbz
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

import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StreamReceiver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Tests for class {@link YamlDecoder}.
 *
 * @author Jens Wille
 *
 */
public final class YamlDecoderTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private StreamReceiver receiver;

    public YamlDecoderTest() {
    }

    @Test
    public void testShouldProcessEmptyStrings() {
        assertDecode(
                i -> {
                    i.process("");
                },
                o -> {
                }
        );
    }

    @Test
    public void testShouldProcessRecords() {
        assertDecode(
                i -> {
                    i.process(
                            "\"lit1\": \"value 1\"\n" +
                            "\" ent1\":\n" +
                            "  \"lit2\": \"value {x}\"\n" +
                            "  \"lit\\\\3\": \"value 2 \"\n" +
                            "\"lit4\": \"value '3'\"\n" +
                            "\"lit5\": null\n"
                    );
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("lit1", "value 1");
                    o.get().startEntity(" ent1");
                    o.get().literal("lit2", "value {x}");
                    o.get().literal("lit\\3", "value 2 ");
                    o.get().endEntity();
                    o.get().literal("lit4", "value '3'");
                    o.get().literal("lit5", null);
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testShouldProcessArrays() {
        assertDecode(
                i -> {
                    i.process(
                            "\"arr1\": [\"val1\",\"val2\"]\n" +
                            "\"arr2\":\n" +
                            "- \"lit1\": \"val1\"\n" +
                            "  \"lit2\": \"val2\"\n" +
                            "- \"lit3\": \"val3\"\n" +
                            "\"arr3\":\n" +
                            "- - \"lit4\": \"val4\"\n" +
                            "- - \"lit5\": \"val5\"\n"
                    );
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    o.get().startEntity("arr1[]");
                    o.get().literal("1", "val1");
                    o.get().literal("2", "val2");
                    o.get().endEntity();
                    o.get().startEntity("arr2[]");
                    o.get().startEntity("1");
                    o.get().literal("lit1", "val1");
                    o.get().literal("lit2", "val2");
                    o.get().endEntity();
                    o.get().startEntity("2");
                    o.get().literal("lit3", "val3");
                    f.apply(2).endEntity();
                    o.get().startEntity("arr3[]");
                    o.get().startEntity("1[]");
                    o.get().startEntity("1");
                    o.get().literal("lit4", "val4");
                    f.apply(2).endEntity();
                    o.get().startEntity("2[]");
                    o.get().startEntity("1");
                    o.get().literal("lit5", "val5");
                    f.apply(3).endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testShouldProcessConcatenatedRecords() {
        assertDecode(
                i -> {
                    i.process(
                            "\"lit\": \"record 1\"\n" +
                            "---\n" +
                            "\"lit\": \"record 2\"\n"
                    );
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("lit", "record 1");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("lit", "record 2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testShouldNotProcessRecordsInArrayField() {
        assertDecode(
                i -> {
                    i.process(
                            "\"data\":\n" +
                            "- \"lit\": \"record 1\"\n" +
                            "- \"lit\": \"record 2\"\n"
                    );
                },
                (o, f) -> {
                    o.get().startRecord("1");
                    o.get().startEntity("data[]");
                    o.get().startEntity("1");
                    o.get().literal("lit", "record 1");
                    o.get().endEntity();
                    o.get().startEntity("2");
                    o.get().literal("lit", "record 2");
                    f.apply(2).endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testRootArrayNoRecordPath() {
        assertException("Unexpected token 'START_ARRAY'",
                i -> {
                    i.process(
                            "- \"lit\": \"record 1\"\n" +
                            "- \"lit\": \"record 2\"\n"
                    );
                }
        );
    }

    @Test
    public void testShouldProcessMultipleRecords() {
        assertDecode(
                i -> {
                    i.process("\"lit\": \"record 1\"\n");
                    i.process("\"lit\": \"record 2\"\n");
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("lit", "record 1");
                    o.get().endRecord();
                    o.get().startRecord("2");
                    o.get().literal("lit", "record 2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testShouldOnlyParseObjects() {
        assertException("Unexpected token 'VALUE_NULL'",
                i -> {
                    i.process("null");
                }
        );
    }

    @Test
    public void testShouldNotParseMalformedObjects() {
        assertException("Unexpected token 'VALUE_STRING'",
                i -> {
                    i.process("\"lit\":\"value\"");
                }
        );
    }

    @Test
    public void testShouldNotParseTrailingContent() {
        assertException("Unexpected token 'VALUE_NULL'",
                i -> {
                    i.process("\"lit\": \"value\"\n---\nnull");
                }
        );
    }

    @Test
    public void testShouldNotParseTrailingGarbage() {
        assertException("Unexpected token 'VALUE_STRING'",
                i -> {
                    i.process("\"lit\": \"value\"\n---\nXXX");
                }
        );
    }

    @Test
    public void testShouldNotParseCStyleComments() {
        assertDecode(
                i -> {
                    i.process("//\"lit\": \"value\"");
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("//\"lit\"", "value");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testShouldParseShellStyleComments() {
        assertDecode(
                i -> {
                    i.process("#\"lit\": \"value\"");
                },
                o -> {
                }
        );
    }

    @Test
    public void testShouldNotParseInlineCStyleComments() {
        assertDecode(
                i -> {
                    i.process("\"lit\": /*comment*/\"value\"");
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("lit", "/*comment*/\"value\"");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void testShouldParseInlineShellStyleComments() {
        assertDecode(
                i -> {
                    i.process("\"lit\": #\"value\"");
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("lit", null);
                    o.get().endRecord();
                }
        );
    }

    private void assertException(final String message, final Consumer<YamlDecoder> in) {
        exception.expect(MetafactureException.class);
        exception.expectMessage(message);

        assertDecode(in, o -> { });
    }

    private void assertDecode(final Consumer<YamlDecoder> in, final Consumer<Supplier<StreamReceiver>> out) {
        assertDecode(in, (s, f) -> out.accept(s));
    }

    private void assertDecode(final Consumer<YamlDecoder> in, final BiConsumer<Supplier<StreamReceiver>, IntFunction<StreamReceiver>> out) {
        final InOrder ordered = Mockito.inOrder(receiver);

        final YamlDecoder yamlDecoder = new YamlDecoder();
        yamlDecoder.setReceiver(receiver);
        in.accept(yamlDecoder);

        try {
            out.accept(() -> ordered.verify(receiver), i -> ordered.verify(receiver, Mockito.times(i)));

            ordered.verifyNoMoreInteractions();
            Mockito.verifyNoMoreInteractions(receiver);
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

}
