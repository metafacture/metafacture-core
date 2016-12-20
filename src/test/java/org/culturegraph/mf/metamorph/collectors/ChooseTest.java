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
 * Tests for class {@link Choose}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class ChooseTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldChooseValueOfTopMostFiringStatement() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <choose>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </choose>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "A");
    metamorph.literal("data2", "B");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("data1", "B");
    metamorph.literal("data2", "A");
    metamorph.endRecord();
    metamorph.startRecord("3");
    metamorph.literal("data2", "C");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("data1", "A");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).literal("data1", "B");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("3");
    ordered.verify(receiver).literal("data2", "C");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldMakeChooseDecisionOnFlushEvent() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <choose flushWith='entity'>")
        .with("    <data source='entity.data1' />")
        .with("    <data source='entity.data2' />")
        .with("  </choose>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data1", "A");
    metamorph.literal("data2", "B");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data1", "B");
    metamorph.literal("data2", "A");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data2", "C");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("dataX", "X");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("entity.data1", "A");
    ordered.verify(receiver).literal("entity.data1", "B");
    ordered.verify(receiver).literal("entity.data2", "C");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void issue110_shouldOutputFallBackIfFlushedWithEntity() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <choose name='chosen' flushWith='record|entity'>")
        .with("    <data source='entity.data1' />")
        .with("    <data source='L' />")
        .with("  </choose>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("L", "V");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("chosen", "V");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void issue210_issue49_shouldRepeatedlyEmitNamedValueIfResetIsFalse() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <choose flushWith='flush' reset='false'>")
        .with("    <data source='lit1' />")
        .with("  </choose>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("lit1", "data1");
    metamorph.literal("flush", "first");
    metamorph.literal("flush", "second");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver, times(2)).literal("lit1", "data1");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void issue210_shouldResetAfterEmittingNamedValueIfResetIsTrue() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <choose flushWith='flush' reset='true'>")
        .with("    <data source='lit1' />")
        .with("  </choose>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("lit1", "data1");
    metamorph.literal("flush", "first");
    metamorph.literal("flush", "second");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver, times(1)).literal("lit1", "data1");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void issue210_shouldResetAfterEmittingNamedValueByDefault() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <choose flushWith='flush'>")
        .with("    <data source='lit1' />")
        .with("  </choose>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("lit1", "data1");
    metamorph.literal("flush", "first");
    metamorph.literal("flush", "second");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver, times(1)).literal("lit1", "data1");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void issue250_shouldResetOnEntityChangeIfSameEntityIsTrue() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <choose sameEntity='true'>")
        .with("    <data source='entity.lit1' />")
        .with("    <data source='entity.lit2' />")
        .with("  </choose>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("lit1", "data1");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("lit2", "data2");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("entity.lit2", "data2");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void issue250_shouldNotResetOnEntityChangeIfSameEntityIsFalse() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <choose sameEntity='false'>")
        .with("    <data source='entity.lit1' />")
        .with("    <data source='entity.lit2' />")
        .with("  </choose>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("lit1", "data1");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("lit2", "data2");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("entity.lit1", "data1");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void issue250_shouldNotResetOnEntityChangeByDefault() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <choose>")
        .with("    <data source='entity.lit1' />")
        .with("    <data source='entity.lit2' />")
        .with("  </choose>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("lit1", "data1");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("lit2", "data2");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("entity.lit1", "data1");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
