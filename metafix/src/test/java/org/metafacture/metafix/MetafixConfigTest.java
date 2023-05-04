/*
 * Copyright 2023 Fabian Steeg, hbz
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

package org.metafacture.metafix;

import org.metafacture.framework.StreamReceiver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

/**
 * Tests Metafix custom configuration options.
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
public class MetafixConfigTest {

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixConfigTest() {
    }

    @Test
    public void setRepeatedFieldsToEntitiesAndSetEntityMemberName() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setRepeatedFieldsToEntities(true);
                i.setEntityMemberName("*");

                i.startRecord("1");
                i.literal("name", "max");
                i.literal("name", "mo");
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("name");
                o.get().literal("*", "max");
                o.get().literal("*", "mo");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void setRepeatedFieldsToEntitiesAndSetEntityMemberNameWithNumericalSubfield() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setRepeatedFieldsToEntities(true);
                i.setEntityMemberName("*");

                i.startRecord("1");
                i.startEntity("1001 ");
                i.literal("1", "max");
                i.literal("1", "mo");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("1001 ");
                o.get().startEntity("1");
                o.get().literal("*", "max");
                o.get().literal("*", "mo");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void setRepeatedFieldsToEntitiesWithNumericalSubfields() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setRepeatedFieldsToEntities(true);

                i.startRecord("1");
                i.startEntity("1001 ");
                i.literal("1", "max");
                i.literal("1", "mo");
                i.endEntity();
                i.endRecord();
            },
            (o, f) -> {
                o.get().startRecord("1");
                o.get().startEntity("1001 ");
                o.get().startEntity("1");
                o.get().literal("1", "max");
                o.get().literal("2", "mo");
                f.apply(2).endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void setEntityMemberNameNoArrayMarkerOrEntity() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setEntityMemberName("*"); // no arrays or entities, no effect

                i.startRecord("1");
                i.startEntity("1001 ");
                i.literal("1", "max");
                i.literal("1", "mo");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("1001 ");
                o.get().literal("1", "max");
                o.get().literal("1", "mo");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

    @Test
    public void setEntityMemberNameWithArrayMarker() {
        MetafixTestHelpers.assertFix(streamReceiver, Arrays.asList(
                "nothing()"
            ),
            i -> {
                i.setEntityMemberName("*");

                i.startRecord("1");
                i.startEntity("1001 []");
                i.literal("1", "max");
                i.literal("1", "mo");
                i.endEntity();
                i.endRecord();
            },
            o -> {
                o.get().startRecord("1");
                o.get().startEntity("1001 []");
                o.get().literal("*", "max");
                o.get().literal("*", "mo");
                o.get().endEntity();
                o.get().endRecord();
            }
        );
    }

}
