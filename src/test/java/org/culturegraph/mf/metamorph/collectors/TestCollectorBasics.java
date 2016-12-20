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
 * Tests the basic functionality of Metamorph collectors.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class TestCollectorBasics {

  // TODO: Can this be changed into a JUnit test for AbstractCollect?

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldSupportNestedCollectors() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='d' value='${1}${c}'>")
        .with("    <data source='d1' name='1' />")
        .with("    <combine name='c' value='${2}${3}'>")
        .with("      <data source='d2' name='2' />")
        .with("      <data source='d3' name='3' />")
        .with("      <postprocess>")
        .with("        <trim />")
        .with("      </postprocess>")
        .with("    </combine>")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("d1", "a");
    metamorph.literal("d2", "b");
    metamorph.literal("d3", "c ");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("d", "abc");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();  }

  @Test
  public void shouldSupportNestedSameEntity() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='result' value='${value}${ch}' sameEntity='true'>")
        .with("    <data source='rel.value' name='value' />")
        .with("    <choose name='ch' flushWith='rel'>")
        .with("      <data source='rel.ch' />")
        .with("      <data source='rel'>")
        .with("        <constant value='M' />")
        .with("      </data>")
        .with("    </choose>")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("rel");
    metamorph.literal("ch", "b");
    metamorph.literal("value", "a");
    metamorph.endEntity();
    metamorph.startEntity("rel");
    metamorph.literal("value", "B");
    metamorph.endEntity();
    metamorph.startEntity("rel");
    metamorph.literal("ch", "e");
    metamorph.literal("value", "d");
    metamorph.endEntity();
    metamorph.startEntity("rel");
    metamorph.literal("ch", "X");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("result", "ab");
    ordered.verify(receiver).literal("result", "BM");
    ordered.verify(receiver).literal("result", "de");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldAllowUsingAnArbitraryLiteralForFlush() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <concat delimiter='' name='d' flushWith='f'>")
        .with("    <data source='d' />")
        .with("  </concat>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("d", "1");
    metamorph.literal("d", "2");
    metamorph.literal("f", "");
    metamorph.literal("d", "3");
    metamorph.literal("d", "4");
    metamorph.literal("d", "5");
    metamorph.literal("f", "");
    metamorph.literal("d", "6");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("d", "12");
    ordered.verify(receiver).literal("d", "345");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldReceiveFlushingLiteralBeforeFlushEvent() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <concat delimiter='' name='d' flushWith='f'>")
        .with("    <data source='d' />")
        .with("    <data source='f' />")
        .with("  </concat>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("f", "1");
    metamorph.literal("f", "2");
    metamorph.literal("d", "a");
    metamorph.literal("f", "3");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("d", "1");
    ordered.verify(receiver).literal("d", "2");
    ordered.verify(receiver).literal("d", "a3");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
