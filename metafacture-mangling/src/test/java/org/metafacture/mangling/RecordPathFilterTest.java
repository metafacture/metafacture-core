/*
 * Copyright 2021 hbz NRW
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
package org.metafacture.mangling;

import org.junit.Rule;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RecordPathFilterTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    @Test
    public void shouldFilterRootPath() {
        assertFilter(
                i -> {
                    i.startRecord("1");
                    i.startEntity("data");
                    i.literal("lit", "record 1");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("data");
                    i.literal("lit", "record 2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().startEntity("data");
                    o.get().literal("lit", "record 1");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2[1]");
                    o.get().startEntity("data");
                    o.get().literal("lit", "record 2");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFilterSimplePathWithLiterals() {
        assertFilter(
                i -> {
                    i.setPath("data");

                    i.startRecord("1");
                    i.startEntity("data");
                    i.literal("lit", "record 1");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("junk");
                    i.literal("lit", "skipped");
                    i.endEntity();
                    i.startEntity("data");
                    i.literal("lit", "record 2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().literal("lit", "record 1");
                    o.get().endRecord();
                    o.get().startRecord("2[1]");
                    o.get().literal("lit", "record 2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFilterNestedPathWithLiterals() {
        assertFilter(
                i -> {
                    i.setPath("nested.data");

                    i.startRecord("1");
                    i.startEntity("nested");
                    i.startEntity("data");
                    i.literal("lit", "record 1");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("nested");
                    i.startEntity("junk");
                    i.literal("lit", "skipped");
                    i.endEntity();
                    i.startEntity("data");
                    i.literal("lit", "record 2");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().literal("lit", "record 1");
                    o.get().endRecord();
                    o.get().startRecord("2[1]");
                    o.get().literal("lit", "record 2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFilterSimplePathWithEntities() {
        assertFilter(
                i -> {
                    i.setPath("data");

                    i.startRecord("1");
                    i.startEntity("data");
                    i.startEntity("ent1");
                    i.literal("lit", "record 1.1");
                    i.endEntity();
                    i.startEntity("ent2");
                    i.literal("lit", "record 1.2");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("junk");
                    i.literal("lit", "skipped");
                    i.endEntity();
                    i.startEntity("data");
                    i.literal("lit", "record 2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().startEntity("ent1");
                    o.get().literal("lit", "record 1.1");
                    o.get().endEntity();
                    o.get().startEntity("ent2");
                    o.get().literal("lit", "record 1.2");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2[1]");
                    o.get().literal("lit", "record 2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFilterNestedPathWithEntities() {
        assertFilter(
                i -> {
                    i.setPath("nested.data");

                    i.startRecord("1");
                    i.startEntity("nested");
                    i.startEntity("data");
                    i.startEntity("ent1");
                    i.literal("lit", "record 1.1");
                    i.endEntity();
                    i.startEntity("ent2");
                    i.literal("lit", "record 1.2");
                    i.endEntity();
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("nested");
                    i.startEntity("junk");
                    i.literal("lit", "skipped");
                    i.endEntity();
                    i.startEntity("data");
                    i.literal("lit", "record 2");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().startEntity("ent1");
                    o.get().literal("lit", "record 1.1");
                    o.get().endEntity();
                    o.get().startEntity("ent2");
                    o.get().literal("lit", "record 1.2");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2[1]");
                    o.get().literal("lit", "record 2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFilterMultiplePathMatches() {
        assertFilter(
                i -> {
                    i.setPath("data");

                    i.startRecord("1");
                    i.startEntity("data");
                    i.literal("lit", "record 1.1");
                    i.endEntity();
                    i.startEntity("data");
                    i.literal("lit", "record 1.2");
                    i.endEntity();
                    i.startEntity("junk");
                    i.literal("lit", "skipped");
                    i.endEntity();
                    i.startEntity("data");
                    i.literal("lit", "record 1.3");
                    i.startEntity("ent1");
                    i.literal("lit", "record 1.3");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("data");
                    i.startEntity("ent1");
                    i.literal("lit", "record 2.1");
                    i.endEntity();
                    i.endEntity();
                    i.startEntity("data");
                    i.literal("lit", "record 2.2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().literal("lit", "record 1.1");
                    o.get().endRecord();
                    o.get().startRecord("1[2]");
                    o.get().literal("lit", "record 1.2");
                    o.get().endRecord();
                    o.get().startRecord("1[3]");
                    o.get().literal("lit", "record 1.3");
                    o.get().startEntity("ent1");
                    o.get().literal("lit", "record 1.3");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2[1]");
                    o.get().startEntity("ent1");
                    o.get().literal("lit", "record 2.1");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2[2]");
                    o.get().literal("lit", "record 2.2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotFilterNonMatchingRecord() {
        assertFilter(
                i -> {
                    i.setPath("data");

                    i.startRecord("1");
                    i.startEntity("data");
                    i.literal("lit", "record 1");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("other-data");
                    i.literal("lit", "record 2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().literal("lit", "record 1");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFilterPathWithEntitySeparator() {
        assertFilter(
                i -> {
                    i.setPath("dotted.data");

                    i.startRecord("1");
                    i.startEntity("dotted.data");
                    i.literal("lit", "record 1.1");
                    i.endEntity();
                    i.startEntity("dotted");
                    i.startEntity("data");
                    i.literal("lit", "record 1.2");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().literal("lit", "record 1.1");
                    o.get().endRecord();
                    o.get().startRecord("1[2]");
                    o.get().literal("lit", "record 1.2");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFilterPathWithSpecifiedEntitySeparator() {
        assertFilter(
                i -> {
                    i.setPath("dotted.data");
                    i.setEntitySeparator("/");

                    i.startRecord("1");
                    i.startEntity("dotted.data");
                    i.literal("lit", "record 1.1");
                    i.endEntity();
                    i.startEntity("dotted");
                    i.startEntity("data");
                    i.literal("lit", "record 1.2");
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().literal("lit", "record 1.1");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldFilterNestedPathWithSpecifiedEntitySeparator() {
        assertFilter(
                i -> {
                    i.setPath("nested/dotted.data");
                    i.setEntitySeparator("/");

                    i.startRecord("1");
                    i.startEntity("nested");
                    i.startEntity("dotted.data");
                    i.literal("lit", "record 1.1");
                    i.endEntity();
                    i.startEntity("dotted");
                    i.startEntity("data");
                    i.literal("lit", "record 1.2");
                    i.endEntity();
                    i.endEntity();
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().literal("lit", "record 1.1");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldRespectRecordIdFormat() {
        assertFilter(
                i -> {
                    i.setRecordIdFormat("%s.%d");

                    i.startRecord("1");
                    i.startEntity("data");
                    i.literal("lit", "record 1");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.startEntity("data");
                    i.literal("lit", "record 2");
                    i.endEntity();
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1.1");
                    o.get().startEntity("data");
                    o.get().literal("lit", "record 1");
                    o.get().endEntity();
                    o.get().endRecord();
                    o.get().startRecord("2.1");
                    o.get().startEntity("data");
                    o.get().literal("lit", "record 2");
                    o.get().endEntity();
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldNotFilterLiteralPath() {
        assertFilter(
                i -> {
                    i.setPath("data");

                    i.startRecord("1");
                    i.startEntity("data");
                    i.literal("lit", "record 1");
                    i.endEntity();
                    i.endRecord();
                    i.startRecord("2");
                    i.literal("data", "record 2");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1[1]");
                    o.get().literal("lit", "record 1");
                    o.get().endRecord();
                }
        );
    }

    private void assertFilter(final Consumer<RecordPathFilter> in, final Consumer<Supplier<StreamReceiver>> out) {
        final RecordPathFilter recordPathFilter = new RecordPathFilter();
        recordPathFilter.setReceiver(receiver);

        TestHelpers.assertProcess(receiver, () -> in.accept(recordPathFilter), out);
    }

}
