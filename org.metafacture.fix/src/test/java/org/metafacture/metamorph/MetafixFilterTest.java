/*
 * Copyright 2013, 2021 Deutsche Nationalbibliothek and others
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;

/**
 * Tests for class {@link MetafixFilter}.
 *
 * @author Fabian Steeg
 *
 */
@ExtendWith(MockitoExtension.class)
public final class MetafixFilterTest {

    private static final String VAL = "val";

    @RegisterExtension
    private MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver streamReceiver;

    public MetafixFilterTest() { }

    @Test
    public void shouldFilterRecords() {
        final MetafixFilter filter = filter("map(a)");

        filter.startRecord("1");
        filter.literal("a", VAL);
        filter.endRecord();

        filter.startRecord("2");
        filter.literal("b", VAL);
        filter.endRecord();

        final InOrder ordered = Mockito.inOrder(streamReceiver);
        ordered.verify(streamReceiver).startRecord("1");
        ordered.verify(streamReceiver).literal("a", VAL);
        ordered.verify(streamReceiver).endRecord();
        ordered.verifyNoMoreInteractions();
    }

    private MetafixFilter filter(final String... filter) {
        return filter(Collections.emptyMap(), filter);
    }

    private MetafixFilter filter(final Map<String, String> vars, final String... fix) {
        final String fixString = String.join("\n", fix);
        System.out.println("\nFix filter string: " + fixString);

        MetafixFilter filter = null;
        try {
            filter = new MetafixFilter(fixString, vars);
            filter.setReceiver(streamReceiver);
        }
        catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
        return filter;
    }

}
