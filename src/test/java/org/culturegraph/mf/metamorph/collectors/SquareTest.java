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
 * Tests for class {@link Square}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class SquareTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldEmitSquaresOfInputValues() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <square delimiter=',' name='square' prefix='{' postfix='}'>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </square>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "b");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "c");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("data1", "1");
    metamorph.literal("data1", "2");
    metamorph.literal("data2", "3");
    metamorph.literal("data2", "4");
    metamorph.literal("data2", "5");
    metamorph.literal("data2", "6");
    metamorph.literal("data2", "7");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("square", "{a,c}");
    ordered.verify(receiver).literal("square", "{b,c}");
    ordered.verify(receiver).literal("square", "{a,b}");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).literal("square", "{1,7}");
    ordered.verify(receiver).literal("square", "{2,7}");
    ordered.verify(receiver).literal("square", "{3,7}");
    ordered.verify(receiver).literal("square", "{4,7}");
    ordered.verify(receiver).literal("square", "{5,7}");
    ordered.verify(receiver).literal("square", "{6,7}");
    ordered.verify(receiver).literal("square", "{1,6}");
    ordered.verify(receiver).literal("square", "{2,6}");
    ordered.verify(receiver).literal("square", "{3,6}");
    ordered.verify(receiver).literal("square", "{4,6}");
    ordered.verify(receiver).literal("square", "{5,6}");
    ordered.verify(receiver).literal("square", "{1,5}");
    ordered.verify(receiver).literal("square", "{2,5}");
    ordered.verify(receiver).literal("square", "{3,5}");
    ordered.verify(receiver).literal("square", "{4,5}");
    ordered.verify(receiver).literal("square", "{1,4}");
    ordered.verify(receiver).literal("square", "{2,4}");
    ordered.verify(receiver).literal("square", "{3,4}");
    ordered.verify(receiver).literal("square", "{1,3}");
    ordered.verify(receiver).literal("square", "{2,3}");
    ordered.verify(receiver).literal("square", "{1,2}");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEmitSquaresOnFlushEvent() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <square delimiter=',' name='square' prefix='{' postfix='}' flushWith='d'>")
        .with("    <data source='d.1' />")
        .with("  </square>")
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
    ordered.verify(receiver).literal("square", "{a,b}");
    ordered.verify(receiver).literal("square", "{e,f}");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
