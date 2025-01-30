/*
 * Copyright 2016 Christoph Böhme
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

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests the macro functionality in Metamorph.
 *
 * @author Christoph Böhme
 */
public class TestMetamorphMacros {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private StreamReceiver receiver;

    public TestMetamorphMacros() {
    }

    @Test
    public void shouldReplaceCallMacroWithMacro() {
        TestHelpers.assertMorph(receiver,
                "<macros>" +
                "  <macro name='simple-macro'>" +
                "    <data source='$[in]' name='$[out]' />" +
                "  </macro>" +
                "</macros>" +
                "<rules>" +
                "  <call-macro name='simple-macro' in='in1' out='out1' />" +
                "  <call-macro name='simple-macro' in='in2' out='out2' />" +
                "</rules>",
                i -> {
                    i.startRecord("1");
                    i.literal("in1", "Hawaii");
                    i.literal("in2", "Maui");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");
                    o.get().literal("out1", "Hawaii");
                    o.get().literal("out2", "Maui");
                    o.get().endRecord();
                }
        );
    }

    @Test
    public void shouldAllowCallMacroInEntities() {
        testSingleLiteral(true,
                "<macros>" +
                "  <macro name='simple-macro'>" +
                "    <data source='Honolulu' name='Honolulu' />" +
                "  </macro>" +
                "</macros>" +
                "<rules>" +
                "  <entity name='Hawaii'>" +
                "    <call-macro name='simple-macro' />" +
                "  </entity>" +
                "</rules>"
        );
    }

    @Test
    public void shouldAllowNestedMacros() {
        testSingleLiteral(true,
                "<macros>" +
                "  <macro name='inner-macro'>" +
                "    <data source='$[literal]' />" +
                "  </macro>" +
                "  <macro name='outer-macro'>" +
                "    <entity name='$[entity]'>" +
                "      <call-macro name='inner-macro' literal='Honolulu' />" +
                "    </entity>" +
                "  </macro>" +
                "</macros>" +
                "<rules>" +
                "  <call-macro name='outer-macro' entity='Hawaii' />" +
                "</rules>"
        );
    }

    @Test
    public void shouldAllowoForwardReferencingMacros() {
        testSingleLiteral(true,
                "<macros>" +
                "  <macro name='referencing'>" +
                "    <entity name='Hawaii'>" +
                "      <call-macro name='forward-referenced' />" +
                "    </entity>" +
                "  </macro>" +
                "  <macro name='forward-referenced'>" +
                "    <data source='Honolulu' />" +
                "  </macro>" +
                "</macros>" +
                "<rules>" +
                "  <call-macro name='referencing' />" +
                "</rules>"
        );
    }

    @Test
    public void shouldSupportVariablesInMacroParameters() {
        testSingleLiteral(true,
                "<macros>" +
                "  <macro name='inner-macro'>" +
                "    <data source='$[source]' />" +
                "  </macro>" +
                "  <macro name='outer-macro'>" +
                "    <entity name='Hawaii'>" +
                "      <call-macro name='inner-macro' source='$[literal]' />" +
                "    </entity>" +
                "  </macro>" +
                "</macros>" +
                "<rules>" +
                "  <call-macro name='outer-macro' literal='Honolulu' />" +
                "</rules>"
        );
    }

    @Test
    public void issue227_shouldSupportXincludeForMacros() {
        testSingleLiteral(false,
                "<include href='issue227_should-support-xinclude-for-macros.xml'" +
                "    xmlns='http://www.w3.org/2001/XInclude' />" +
                "<rules>" +
                "  <call-macro name='included-macro' />" +
                "</rules>"
        );
    }

    @Test
    public void shouldSupportXPointer() {
        testSingleLiteral(false,
                "<include href='should-support-xpointer.xml'" +
                "    xmlns='http://www.w3.org/2001/XInclude'" +
                "    xpointer='element(/1/1)' />" +
                "<rules>" +
                "  <call-macro name='included-macro' />" +
                "</rules>"
        );
    }

    private void testSingleLiteral(final boolean withEntity, final String morphDef) {
        TestHelpers.assertMorph(receiver, morphDef,
                i -> {
                    i.startRecord("1");
                    i.literal("Honolulu", "Aloha");
                    i.endRecord();
                },
                o -> {
                    o.get().startRecord("1");

                    if (withEntity) {
                        o.get().startEntity("Hawaii");
                    }

                    o.get().literal("Honolulu", "Aloha");

                    if (withEntity) {
                        o.get().endEntity();
                    }

                    o.get().endRecord();
                }
        );
    }

}
