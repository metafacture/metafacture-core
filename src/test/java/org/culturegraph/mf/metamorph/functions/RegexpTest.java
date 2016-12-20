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
 * Tests for class {@link Regexp}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class RegexpTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldMatchAndReplaceUsingRegularExpressions() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='001.' name='subject'>")
        .with("    <regexp match='.*' format='resource:P${0}' />")
        .with("  </data>")
        .with("  <data source='001.' name='subject'>")
        .with("    <regexp match='.*' format='${1}' />")
        .with("  </data>")
        .with("  <data source='001.' name='subject'>")
        .with("    <regexp match='.*' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("001");
    metamorph.literal("", "184000");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("subject", "resource:P184000");
    ordered.verify(receiver).literal("subject", "");
    ordered.verify(receiver).literal("subject", "184000");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldIgnoreEmptyMatchGroups() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='s'>")
        .with("    <regexp match='aa(bb*)?(cc*)(dd*)' format='${1}${2}${3}' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("s", "aaccdd");
    metamorph.literal("s", "ax");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("s", "ccdd");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
