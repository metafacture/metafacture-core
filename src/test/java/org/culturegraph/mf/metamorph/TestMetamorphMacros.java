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
package org.culturegraph.mf.metamorph;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
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

	private Metamorph metamorph;

	@Test
	public void shouldReplaceCallMacroWithMacro() {
		metamorph = InlineMorph.in(this)
				.with("<macros>")
				.with("  <macro name='simple-macro'>")
				.with("    <data source='$[in]' name='$[out]' />")
				.with("  </macro>")
				.with("</macros>")
				.with("<rules>")
				.with("  <call-macro name='simple-macro' in='in1' out='out1' />")
				.with("  <call-macro name='simple-macro' in='in2' out='out2' />")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.literal("in1", "Hawaii");
		metamorph.literal("in2", "Maui");
		metamorph.endRecord();

		verify(receiver).literal("out1", "Hawaii");
		verify(receiver).literal("out2", "Maui");
	}

	@Test
	public void shouldAllowCallMacroInEntities() {
		metamorph = InlineMorph.in(this)
				.with("<macros>")
				.with("  <macro name='simple-macro'>")
				.with("    <data source='Honolulu' name='Honolulu' />")
				.with("  </macro>")
				.with("</macros>")
				.with("<rules>")
				.with("  <entity name='Hawaii'>")
				.with("    <call-macro name='simple-macro' />")
				.with("  </entity>")
				.with("</rules>")
				.createConnectedTo(receiver);

		processRecordWithSingleLiteral();

		verifyEntityWithSingleLiteral();
	}

	@Test
	public void shouldAllowNestedMacros() {
		metamorph = InlineMorph.in(this)
				.with("<macros>")
				.with("  <macro name='inner-macro'>")
				.with("    <data source='$[literal]' />")
				.with("  </macro>")
				.with("  <macro name='outer-macro'>")
				.with("    <entity name='$[entity]'>")
				.with("      <call-macro name='inner-macro' literal='Honolulu' />")
				.with("    </entity>")
				.with("  </macro>")
				.with("</macros>")
				.with("<rules>")
				.with("  <call-macro name='outer-macro' entity='Hawaii' />")
				.with("</rules>")
				.createConnectedTo(receiver);

		processRecordWithSingleLiteral();

		verifyEntityWithSingleLiteral();
	}

	@Test
	public void shouldAllowoForwardReferencingMacros() {
		metamorph = InlineMorph.in(this)
				.with("<macros>")
				.with("  <macro name='referencing'>")
				.with("    <entity name='Hawaii'>")
				.with("      <call-macro name='forward-referenced' />")
				.with("    </entity>")
				.with("  </macro>")
				.with("  <macro name='forward-referenced'>")
				.with("    <data source='Honolulu' />")
				.with("  </macro>")
				.with("</macros>")
				.with("<rules>")
				.with("  <call-macro name='referencing' />")
				.with("</rules>")
				.createConnectedTo(receiver);

		processRecordWithSingleLiteral();

		verifyEntityWithSingleLiteral();
	}

	@Test
	public void shouldSupportVariablesInMacroParameters() {
		metamorph = InlineMorph.in(this)
				.with("<macros>")
				.with("  <macro name='inner-macro'>")
				.with("    <data source='$[source]' />")
				.with("  </macro>")
				.with("  <macro name='outer-macro'>")
				.with("    <entity name='Hawaii'>")
				.with("      <call-macro name='inner-macro' source='$[literal]' />")
				.with("    </entity>")
				.with("  </macro>")
				.with("</macros>")
				.with("<rules>")
				.with("  <call-macro name='outer-macro' literal='Honolulu' />")
				.with("</rules>")
				.createConnectedTo(receiver);

		processRecordWithSingleLiteral();

		verifyEntityWithSingleLiteral();
	}

	@Test
	public void issue227_shouldSupportXincludeForMacros() {
		metamorph = InlineMorph.in(this)
				.with("<include href='issue227_should-support-xinclude-for-macros.xml'")
				.with("    xmlns='http://www.w3.org/2001/XInclude' />")
				.with("<rules>")
				.with("  <call-macro name='included-macro' />")
				.with("</rules>")
				.createConnectedTo(receiver);

		processRecordWithSingleLiteral();

		verify(receiver).literal("Honolulu", "Aloha");
	}

	private void processRecordWithSingleLiteral() {
		metamorph.startRecord("1");
		metamorph.literal("Honolulu", "Aloha");
		metamorph.endRecord();
	}

	private void verifyEntityWithSingleLiteral() {
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startEntity("Hawaii");
		ordered.verify(receiver).literal("Honolulu", "Aloha");
		ordered.verify(receiver).endEntity();
	}

}
