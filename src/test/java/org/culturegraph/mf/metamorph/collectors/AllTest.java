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
 * Tests for class {@link All}.
 *
 * @author Christoph Böhme
 */
public final class AllTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldFireOnlyIfAllElementsFire() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <all>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </all>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "A");
    metamorph.literal("data2", "B");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("data2", "C");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("", "true");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldSupportUserdefinedNameAndValue() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <all name='ALL' value='found all'>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </all>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "A");
    metamorph.literal("data2", "B");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("ALL", "found all");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldFireAgainIfValueChangesAndResetIsFalse() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <all>")
        .with("    <data source='entity.data1' />")
        .with("    <data source='entity.data2' />")
        .with("  </all>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data1", "A");
    metamorph.literal("data2", "B");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data2", "C");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver, times(2)).literal("", "true");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldNotFireAgainIfOnlyOneValueChangesAndResetIsTrue() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <all reset='true'>")
        .with("    <data source='entity.data1' />")
        .with("    <data source='entity.data2' />")
        .with("  </all>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data1", "A");
    metamorph.literal("data2", "B");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data2", "B");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("", "true");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldNotFireIfFlushingAnIncompleteCollection() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <all flushWith='entity'>")
        .with("    <data source='entity.data1' />")
        .with("    <data source='entity.data2' />")
        .with("  </all>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data1", "A");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldResetWhenEntityChangesIfSameEntity() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <all sameEntity='true'>")
        .with("    <data source='entity.data2' />")
        .with("    <data source='entity.data1' />")
        .with("  </all>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data2", "A");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data1", "A");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

}
