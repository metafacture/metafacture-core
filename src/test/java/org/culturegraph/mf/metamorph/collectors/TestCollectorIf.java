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
 * Tests the {@code <if>} statement in collectors.
 *
 * @author Christoph Böhme
 */
public final class TestCollectorIf {

  // TODO: Can this be changed into a JUnit test for AbstractCollect?

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldOnlyFireIfConditionIsMet() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='combined' value='${data1}-${data2}'>")
        .with("    <if>")
        .with("      <data source='data3' />")
        .with("    </if>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "b");
    metamorph.literal("data3", "c");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "b");
    metamorph.literal("data4", "c");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("combined", "a-b");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldAllowToUseSameSourceInbodyAndCondition() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='combined' value='${data1}-${data2}'>")
        .with("    <if>")
        .with("      <data source='data2'>")
        .with("        <equals string='b' />")
        .with("      </data>")
        .with("    </if>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "b");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "c");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("combined", "a-b");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldAllowQuantorsInIfStatements() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='combined' value='${data1}-${data2}'>")
        .with("    <if>")
        .with("      <any>")
        .with("        <data source='data3' />")
        .with("        <data source='data4' />")
        .with("      </any>")
        .with("    </if>")
        .with("    <data source='data1' />")
        .with("    <data source='data2' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "b");
    metamorph.literal("data3", "c");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "d");
    metamorph.literal("data4", "c");
    metamorph.endRecord();
    metamorph.startRecord("3");
    metamorph.literal("data1", "a");
    metamorph.literal("data2", "b");
    metamorph.literal("data5", "c");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("combined", "a-b");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).literal("combined", "a-d");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("3");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldResetConditionWithCollector() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='result' value='${VAL}' reset='true'>")
        .with("    <if>")
        .with("      <data source='entity.data2' />")
        .with("    </if>")
        .with("    <data source='entity.data1' name='VAL' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data1", "output");
    metamorph.literal("data2", "X");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data1", "no-output");
    metamorph.literal("data3", "X");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("result", "output");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldResetConditionWithCollectorOnFlushWith() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='result' value='${VAL1}${VAL2}' reset='true' flushWith='entity'>")
        .with("    <if>")
        .with("      <data source='entity.data2' />")
        .with("    </if>")
        .with("    <data source='entity.data1' name='VAL1' />")
        .with("    <data source='entity.data4' name='VAL2' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data1", "output");
    metamorph.literal("data2", "X");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data1", "no-output");
    metamorph.literal("data3", "X");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("result", "output");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldResetConditionWithCollectorOnSameEntity() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='result' value='${VAL1}+${VAL2}' sameEntity='true'>")
        .with("    <if>")
        .with("      <data source='entity.data2' />")
        .with("    </if>")
        .with("    <data source='entity.data1' name='VAL1' />")
        .with("    <data source='entity.data4' name='VAL2' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("data1", "output");
    metamorph.literal("data2", "X");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("data1", "no-output");
    metamorph.literal("data3", "X");
    metamorph.literal("data4", "extra-output");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldResetOnFlushWithIfConditionWasNotMet() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='result' value='${V1}${V2}' flushWith='entity' reset='true'>")
        .with("    <if>")
        .with("      <data source='entity.condition'>")
        .with("        <equals string='true' />")
        .with("      </data>")
        .with("    </if>")
        .with("    <data source='entity.literal1' name='V1' />")
        .with("    <data source='entity.literal2' name='V2' />")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("entity");
    metamorph.literal("condition", "false");
    metamorph.literal("literal1", "value1");
    metamorph.endEntity();
    metamorph.startEntity("entity");
    metamorph.literal("condition", "true");
    metamorph.literal("literal2", "value2");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("result", "value2");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
