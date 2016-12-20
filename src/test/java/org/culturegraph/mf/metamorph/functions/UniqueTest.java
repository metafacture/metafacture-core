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
import static org.mockito.Mockito.times;

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
 * Tests for class {@link Unique}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class UniqueTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldAllowSelectingTheUniqueScope() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='data' name='inRecord'>")
        .with("    <unique />")
        .with("  </data>")
        .with("  <data source='e.data' name='inEntity'>")
        .with("    <unique in='entity' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("e");
    metamorph.literal("data", "d");
    metamorph.literal("data", "d");
    metamorph.endEntity();
    metamorph.startEntity("e");
    metamorph.literal("data", "d");
    metamorph.literal("data", "d");
    metamorph.endEntity();
    metamorph.literal("data", "d");
    metamorph.literal("data", "d");
    metamorph.literal("data", "d");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.startEntity("e");
    metamorph.literal("data", "d");
    metamorph.literal("data", "d");
    metamorph.endEntity();
    metamorph.startEntity("e");
    metamorph.literal("data", "d");
    metamorph.literal("data", "d");
    metamorph.endEntity();
    metamorph.literal("data", "d");
    metamorph.literal("data", "d");
    metamorph.literal("data", "d");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver, times(2)).literal("inEntity", "d");
    ordered.verify(receiver).literal("inRecord", "d");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver, times(2)).literal("inEntity", "d");
    ordered.verify(receiver).literal("inRecord", "d");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldAllowSelectingTheUniquePart() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <group name='name'>")
        .with("    <group>")
        .with("      <data source='data1' />")
        .with("      <data source='data2' />")
        .with("      <postprocess>")
        .with("        <unique part='name' />")
        .with("      </postprocess>")
        .with("    </group>")
        .with("  </group>")
        .with("  <group name='value'>")
        .with("    <group>")
        .with("      <data source='data1' />")
        .with("      <data source='data2' />")
        .with("      <postprocess>")
        .with("        <unique part='value' />")
        .with("      </postprocess>")
        .with("    </group>")
        .with("  </group>")
        .with("  <group name='both'>")
        .with("    <group>")
        .with("      <data source='data1' />")
        .with("      <data source='data2' />")
        .with("      <postprocess>")
        .with("        <unique part='name-value' />")
        .with("      </postprocess>")
        .with("    </group>")
        .with("  </group>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "d1");
    metamorph.literal("data1", "d1");
    metamorph.literal("data1", "d2");
    metamorph.literal("data1", "d2");
    metamorph.literal("data2", "d2");
    metamorph.literal("data2", "d2");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("name", "d1");
    ordered.verify(receiver).literal("value", "d1");
    ordered.verify(receiver).literal("both", "d1");
    ordered.verify(receiver).literal("value", "d2");
    ordered.verify(receiver).literal("both", "d2");
    ordered.verify(receiver).literal("name", "d2");
    ordered.verify(receiver).literal("both", "d2");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
