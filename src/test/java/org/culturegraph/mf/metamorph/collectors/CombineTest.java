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
 * Tests for class {@link Combine}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class CombineTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldCombineTwoValues() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='combination' value='${one}${two}'>")
        .with("    <data source='data2' name='one' />")
        .with("    <data source='data1' name='two' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "b");
    metamorph.literal("data2", "c");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("combination", "ba");
    ordered.verify(receiver).literal("combination", "ca");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldOnlyCombineValuesFromTheSameEntityIfSet() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='combination' value='${A}${B}' sameEntity='true'>")
        .with("    <data source='entity.data1' name='B' />")
        .with("    <data source='entity.data2' name='A' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data1", "b");
    metamorph.literal("data2", "a");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data2", "c");
    metamorph.literal("data2", "d");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data1", "e");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data2", "f");
    metamorph.literal("data1", "g");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("combination", "ab");
    ordered.verify(receiver).literal("combination", "fg");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldResetCombinedValueIfResetIsTrue() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='combination' value='${A}${B}' reset='true'>")
        .with("    <data source='data1' name='B' />")
        .with("    <data source='data2' name='A' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "b");
    metamorph.literal("data2", "a");
    metamorph.literal("data2", "c");
    metamorph.literal("data2", "d");
    metamorph.literal("data1", "e");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("combination", "ab");
    ordered.verify(receiver).literal("combination", "de");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEmitCurrentValueOnFlushEvent() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='combi' value='${one}${two}' flushWith='e' reset='true'>")
        .with("    <data source='e.l' name='one' />")
        .with("    <data source='e.m' name='two' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("e");
    metamorph.literal("l", "1");
    metamorph.endEntity();
    metamorph.startEntity("e");
    metamorph.literal("l", "2");
    metamorph.literal("m", "2");
    metamorph.endEntity();
    metamorph.startEntity("e");
    metamorph.literal("l", "3");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("combi", "1");
    ordered.verify(receiver).literal("combi", "22");
    ordered.verify(receiver).literal("combi", "3");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldPostprocessCombinedValue() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='outLit' value='${V}' flushWith='record'>")
        .with("    <data name='V' source='inLit' />")
        .with("    <postprocess>")
        .with("      <case to='upper' />")
        .with("    </postprocess>")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("inLit", "value");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("outLit", "VALUE");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
