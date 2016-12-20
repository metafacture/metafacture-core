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
 * Tests for class {@link Any}.
 *
 * @author Christoph Böhme
 */
public final class AnyTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldFireOnlyIfAtLeastOneElementFires() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <any>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </any>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "A");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("data2", "C");
    metamorph.endRecord();
    metamorph.startRecord("3");
    metamorph.literal("data3", "C");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("", "true");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).literal("", "true");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("3");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldSupportUserdefinedNameAndValue() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <any name='ANY' value='found one'>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </any>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "A");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("ANY", "found one");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldNotFireAgainIfAdditionalValueIsReceivedAndResetIsFalse() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <any>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </any>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "A");
    metamorph.literal("data2", "B");
    metamorph.literal("data2", "C");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("", "true");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldFireAgainIfAdditionalValueIsReceivedAndResetIsTrue() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <any reset='true'>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </any>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "A");
    metamorph.literal("data2", "B");
    metamorph.literal("data2", "C");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver, times(3)).literal("", "true");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldFireAgainAfterFlushing() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <any flushWith='entity'>")
        .with("    <data source='entity.data1' />")
        .with("    <data source='entity.data2' />")
        .with("  </any>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data1", "A");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data2", "B");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver, times(2)).literal("", "true");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldNotFireIfFlushingAnUntriggeredCollection() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <any flushWith='entity'>")
        .with("    <data source='entity.data1' />")
        .with("    <data source='entity.data2' />")
        .with("  </any>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data3", "A");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).endRecord();
	  ordered.verifyNoMoreInteractions();
  }

}
