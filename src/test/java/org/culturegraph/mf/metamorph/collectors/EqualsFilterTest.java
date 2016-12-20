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
package org.culturegraph.mf.metamorph.collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

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
 * Tests for class {@link EqualsFilter}.
 *
 * @author Thomas Haidlas (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class EqualsFilterTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldEmitValueIfAllReceivedValuesAreEqual() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <equalsFilter name='equalsFiltered' value='${one}'>")
        .with("    <data source='data1' name='one' />")
        .with("    <data source='data2' name='two' />")
        .with("    <data source='data3' name='three' />")
        .with("  </equalsFilter>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "a");
    metamorph.literal("data3", "a");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("equalsFiltered", "a");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEmitNothingIfReceivedValuesDiffer() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <equalsFilter name='equalsFiltered' value='${one}'>")
        .with("    <data source='data1' name='one' />")
        .with("    <data source='data2' name='two' />")
        .with("    <data source='data3' name='three' />")
        .with("  </equalsFilter>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "a");
    metamorph.literal("data3", "b");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver, never()).literal(eq("equalsFiltered"), any());
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldFireIfOnlyASingleValueIsReceived() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <equalsFilter name='equalsFiltered' value='${one}'>")
        .with("    <data source='data1' name='one' />")
        .with("  </equalsFilter>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "a");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("equalsFiltered", "a");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldIgnoreLiteralsNotListedInStatements() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <equalsFilter name='equalsFiltered' value='${one}'>")
        .with("    <data source='data1' name='one' />")
        .with("    <data source='data2' name='two' />")
        .with("  </equalsFilter>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "a");
    metamorph.literal("data3", "b");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("equalsFiltered", "a");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldFireIfValuesInEntityAreEqual() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <equalsFilter name='equalsFiltered' value='${one}'>")
        .with("    <data source='field.data1' name='one' />")
        .with("    <data source='field.data2' name='two' />")
        .with("    <data source='field.data3' name='three' />")
        .with("  </equalsFilter>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("field");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "a");
    metamorph.literal("data3", "a");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("equalsFiltered", "a");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldNotFireIfValuesInEntityAreNotEqual() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <equalsFilter name='equalsFiltered' value='${one}'>")
        .with("    <data source='field.data1' name='one' />")
        .with("    <data source='field.data2' name='two' />")
        .with("    <data source='field.data3' name='three' />")
        .with("  </equalsFilter>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("field");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "a");
    metamorph.literal("data3", "b");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldFireIfLiteralsInEntitiesAreReceivedThatAreNotListedInStatements() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <equalsFilter name='equalsFiltered' value='${one}'>")
        .with("    <data source='field1.data1' name='one' />")
        .with("    <data source='field1.data2' name='two' />")
        .with("  </equalsFilter>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("field1");
    metamorph.literal("data1", "a");
    metamorph.endEntity();
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.startEntity("field1");
    metamorph.literal("data2", "a");
    metamorph.endEntity();
    metamorph.endRecord();
    metamorph.startRecord("3");
    metamorph.startEntity("field1");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "a");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("3");
    ordered.verify(receiver).literal("equalsFiltered", "a");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
