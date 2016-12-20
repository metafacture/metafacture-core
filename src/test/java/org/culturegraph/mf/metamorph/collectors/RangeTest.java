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
 * Tests for class {@link Range}.
 *
 * @author Christoph Böhme
 */
public final class RangeTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldOutputAllnNmbersbBetweenFirstAndLastInclusive() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <range name='range' flushWith='record'>")
        .with("    <data source='first' />")
        .with("    <data source='last' />")
        .with("  </range>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("first", "1789");
    metamorph.literal("last", "1794");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("range", "1789");
    ordered.verify(receiver).literal("range", "1790");
    ordered.verify(receiver).literal("range", "1791");
    ordered.verify(receiver).literal("range", "1792");
    ordered.verify(receiver).literal("range", "1793");
    ordered.verify(receiver).literal("range", "1794");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldOutputFirstIfLastEqualsFirst() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <range name='range' flushWith='record'>")
        .with("    <data source='first' />")
        .with("    <data source='last' />")
        .with("  </range>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("first", "1989");
    metamorph.literal("last", "1989");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("range", "1989");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldOutputMultipleRanges() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <range name='range' flushWith='record'>")
        .with("    <data source='first' />")
        .with("    <data source='last' />")
        .with("  </range>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("first", "1789");
    metamorph.literal("last", "1792");
    metamorph.literal("first", "1794");
    metamorph.literal("last", "1799");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("range", "1789");
    ordered.verify(receiver).literal("range", "1790");
    ordered.verify(receiver).literal("range", "1791");
    ordered.verify(receiver).literal("range", "1792");
    ordered.verify(receiver).literal("range", "1794");
    ordered.verify(receiver).literal("range", "1795");
    ordered.verify(receiver).literal("range", "1796");
    ordered.verify(receiver).literal("range", "1797");
    ordered.verify(receiver).literal("range", "1798");
    ordered.verify(receiver).literal("range", "1799");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldRemoveDuplicateNumbersFromOverlappingRanges() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <range name='range' flushWith='record'>")
        .with("    <data source='first' />")
        .with("    <data source='last' />")
        .with("  </range>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("first", "1789");
    metamorph.literal("last", "1792");
    metamorph.literal("first", "1790");
    metamorph.literal("last", "1791");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("range", "1789");
    ordered.verify(receiver).literal("range", "1790");
    ordered.verify(receiver).literal("range", "1791");
    ordered.verify(receiver).literal("range", "1792");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldUseUserdefinedIncrement() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <range name='range' increment='3' flushWith='record'>")
        .with("    <data source='first' />")
        .with("    <data source='last' />")
        .with("  </range>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("first", "1789");
    metamorph.literal("last", "1799");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("range", "1789");
    ordered.verify(receiver).literal("range", "1792");
    ordered.verify(receiver).literal("range", "1795");
    ordered.verify(receiver).literal("range", "1798");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldAllowNegativeIncrements() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <range name='range' increment='-3' flushWith='record'>")
        .with("    <data source='first' />")
        .with("    <data source='last' />")
        .with("  </range>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("first", "1799");
    metamorph.literal("last", "1789");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("range", "1799");
    ordered.verify(receiver).literal("range", "1796");
    ordered.verify(receiver).literal("range", "1793");
    ordered.verify(receiver).literal("range", "1790");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
