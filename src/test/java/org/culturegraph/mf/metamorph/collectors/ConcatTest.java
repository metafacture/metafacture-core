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
 * Tests for class {@link Concat}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class ConcatTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldConcatenateValues() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <concat delimiter=', ' name='concat' prefix='{' postfix='}'>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </concat>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "a");
    metamorph.literal("data1", "b");
    metamorph.literal("data2", "c");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("data1", "d");
    metamorph.literal("data1", "e");
    metamorph.literal("data2", "f");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("concat", "{a, b, c}");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).literal("concat", "{d, e, f}");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEmitConcatenatedValueOnFlushEvent() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <concat delimiter=', ' name='concat' prefix='{' postfix='}' flushWith='d' reset='true'>")
        .with("    <data source='d.1' />")
        .with("  </concat>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("d");
    metamorph.literal("1", "a");
    metamorph.literal("1", "b");
    metamorph.endEntity();
    metamorph.startEntity("d");
    metamorph.literal("1", "e");
    metamorph.literal("1", "f");
    metamorph.endEntity();
    metamorph.startEntity("d");
    metamorph.literal("2", "e");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("concat", "{a, b}");
    ordered.verify(receiver).literal("concat", "{e, f}");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEmitEmptyValues() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <concat name='concat' delimiter=', '>")
        .with("    <data source='litA' />")
        .with("    <data source='litB' />")
        .with("  </concat>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("litA", "");
    metamorph.literal("litB", "a");
    metamorph.literal("litA", "b");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("concat", ", a, b");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldReverseConcatenationIfReverseIsTrue() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <concat name='concat' delimiter=', ' reverse='true'>")
        .with("    <data source='litA' />")
        .with("    <data source='litB' />")
        .with("  </concat>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("litA", "1");
    metamorph.literal("litB", "2");
    metamorph.literal("litA", "3");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("concat", "3, 2, 1");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void prefixAndPostfixShouldWorkAsNormalIfReverseIsTrue() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <concat name='concat' delimiter=', ' prefix='(' postfix=')' reverse='true'>")
        .with("    <data source='litA' />")
        .with("    <data source='litB' />")
        .with("  </concat>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("litA", "1");
    metamorph.literal("litB", "2");
    metamorph.literal("litA", "3");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("concat", "(3, 2, 1)");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void issue187_shouldUseEmptyDelimiterAsDefault() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <concat name='concat'>")
        .with("    <data source='lit' />")
        .with("  </concat>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("lit", "data1");
    metamorph.literal("lit", "data2");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("concat", "data1data2");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
