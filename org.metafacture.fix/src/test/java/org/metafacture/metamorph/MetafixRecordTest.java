/*
 * Copyright 2021 Fabian Steeg, hbz
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

import org.metafacture.framework.StreamReceiver;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

/**
 * Tests the non-field-streaming record functionality of Metafix via DSL.
 *
 * See https://github.com/metafacture/metafacture-fix/issues/35
 *
 * @author Fabian Steeg
 */
@ExtendWith(MockitoExtension.class)
public class MetafixRecordTest {

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixRecordTest() {
    }

    @Test
    public void shouldSupportLookingAtOtherFieldsAny() {
        final Metafix metafix = fix(//
                "if any_match('name', '.*University.*')", //
                "  add_field('type', 'Organization')", //
                "end");

        metafix.setRecordMode(true);
        final String name = "name";

        metafix.startRecord("1");
        metafix.literal(name, "Max Musterman");
        metafix.endRecord();

        metafix.startRecord("2");
        metafix.literal(name, "Some University");
        metafix.literal(name, "Filibandrina");
        metafix.endRecord();

        Assert.assertTrue("Some University".matches(".*University.*"));
        final InOrder ordered = Mockito.inOrder(streamReceiver);
        ordered.verify(streamReceiver, Mockito.times(1)).literal("type", "Organization");
    }

    @Test
    public void shouldSupportLookingAtOtherFieldsAll() {
        final Metafix metafix = fix(//
                "if all_match('name', '.*University.*')", //
                "  add_field('type', 'Organization')", //
                "end");

        metafix.setRecordMode(true);
        final String name = "name";

        metafix.startRecord("1");
        metafix.literal(name, "Max Musterman");
        metafix.literal(name, "A University");
        metafix.endRecord();

        metafix.startRecord("2");
        metafix.literal(name, "A University");
        metafix.literal(name, "University Filibandrina");
        metafix.endRecord();

        final InOrder ordered = Mockito.inOrder(streamReceiver);
        ordered.verify(streamReceiver, Mockito.times(1)).literal("type", "Organization");
    }

    private Metafix fix(final String... fix) {
        return MetafixDslTest.fix(Collections.emptyMap(), streamReceiver, fix);
    }

}
