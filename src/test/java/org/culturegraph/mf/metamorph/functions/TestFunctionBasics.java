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
package org.culturegraph.mf.metamorph.functions;

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.metamorph.InlineMorph;
import org.culturegraph.mf.metamorph.Metamorph;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests basic functionality of Metamorph functions.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class TestFunctionBasics {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldSupportFunctionChainingInDataStatements() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='data'>")
        .with("    <trim />")
        .with("    <replace pattern=' ' with='X' />")
        .with("    <replace pattern='a' with='A' />")
        .with("    <regexp match='Abc' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data", " abc ");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("data", " abc ");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("data", "Abc");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).literal("data", "Abc");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldSupportFunctionChainingInEntities() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <choose>")
        .with("    <data source='data'>")
        .with("      <trim />")
        .with("      <replace pattern=' ' with='X' />")
        .with("      <replace pattern='a' with='A' />")
        .with("      <regexp match='Abc' />")
        .with("    </data>")
        .with("  </choose>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data", " abc ");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("data", " abc ");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("data", "Abc");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).literal("data", "Abc");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldUseJavaClassesAsFunctions() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='data'>")
        .with("    <java class='org.culturegraph.mf.metamorph.functions.Compose' prefix='Hula ' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data", "Aloha");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("data", "Hula Aloha");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
