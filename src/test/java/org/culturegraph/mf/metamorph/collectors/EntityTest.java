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
import org.culturegraph.mf.metamorph.Entity;
import org.culturegraph.mf.metamorph.InlineMorph;
import org.culturegraph.mf.metamorph.Metamorph;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Tests for class {@link Entity}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class EntityTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldEmitEntities() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity name='entity'>")
        .with("    <data source='data1' name='l1' />")
        .with("    <data source='data2' name='l2' />")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1x");
    metamorph.literal("data1", "a1");
    metamorph.literal("data1", "a2");
    metamorph.literal("data2", "b");
    metamorph.endRecord();
    metamorph.startRecord("2x");
    metamorph.literal("data2", "c");
    metamorph.literal("data1", "d");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1x");
    ordered.verify(receiver).startEntity("entity");
    ordered.verify(receiver).literal("l1", "a1");
    ordered.verify(receiver).literal("l1", "a2");
    ordered.verify(receiver).literal("l2", "b");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2x");
    ordered.verify(receiver).startEntity("entity");
    ordered.verify(receiver).literal("l2", "c");
    ordered.verify(receiver).literal("l1", "d");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEmitEnityOnFlushEvent() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity name='entity' flushWith='record'>")
        .with("    <data source='d1' name='l1' />")
        .with("    <data source='d2' name='l2' />")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("d1", "a");
    metamorph.literal("d1", "b");
    metamorph.literal("d2", "c");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("d2", "c");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).startEntity("entity");
    ordered.verify(receiver).literal("l1", "a");
    ordered.verify(receiver).literal("l1", "b");
    ordered.verify(receiver).literal("l2", "c");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).startEntity("entity");
    ordered.verify(receiver).literal("l2", "c");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEmitEntityOnEachFlushEvent() {
    metamorph = InlineMorph.in(this)
        .with("<metamorph version='1' entityMarker='.'")
        .with("    xmlns='http://www.culturegraph.org/metamorph'>")
        .with("  <rules>")
        .with("    <entity name='entity' flushWith='E' reset='true'>")
        .with("      <data source='E.d1' name='l1' />")
        .with("      <data source='E.d2' name='l2' />")
        .with("    </entity>")
        .with("  </rules>")
        .with("</metamorph>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.startEntity("E");
    metamorph.literal("d1", "a");
    metamorph.literal("d2", "b");
    metamorph.endEntity();
    metamorph.startEntity("E");
    metamorph.literal("d1", "c");
    metamorph.literal("d2", "d");
    metamorph.endEntity();
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).startEntity("entity");
    ordered.verify(receiver).literal("l1", "a");
    ordered.verify(receiver).literal("l2", "b");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).startEntity("entity");
    ordered.verify(receiver).literal("l1", "c");
    ordered.verify(receiver).literal("l2", "d");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldSupportNestedEntities() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity name='e1' flushWith='record'>")
        .with("    <data source='d1' />")
        .with("    <data source='d2' />")
        .with("    <entity name='e2' flushWith='record'>")
        .with("      <data source='d3' />")
        .with("      <data source='d4' />")
        .with("    </entity>")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("d1", "a");
    metamorph.literal("d2", "b");
    metamorph.literal("d3", "c");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("d1", "d");
    metamorph.literal("d2", "e");
    metamorph.literal("d3", "f");
    metamorph.endRecord();
    metamorph.startRecord("3");
    metamorph.literal("d1", "a");
    metamorph.literal("d3", "c");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).startEntity("e1");
    ordered.verify(receiver).literal("d1", "a");
    ordered.verify(receiver).literal("d2", "b");
    ordered.verify(receiver).startEntity("e2");
    ordered.verify(receiver).literal("d3", "c");
    ordered.verify(receiver, times(2)).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).startEntity("e1");
    ordered.verify(receiver).literal("d1", "d");
    ordered.verify(receiver).literal("d2", "e");
    ordered.verify(receiver).startEntity("e2");
    ordered.verify(receiver).literal("d3", "f");
    ordered.verify(receiver, times(2)).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("3");
    ordered.verify(receiver).startEntity("e1");
    ordered.verify(receiver).literal("d1", "a");
    ordered.verify(receiver).startEntity("e2");
    ordered.verify(receiver).literal("d3", "c");
    ordered.verify(receiver, times(2)).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldSupportMultipleNestedEntities() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity name='uber' flushWith='record'>")
        .with("    <data source='d' name='l' />")
        .with("    <entity name='unter' sameEntity='true'>")
        .with("      <data source='E.d1' name='l' />")
        .with("      <data source='E.d2' name='l' />")
        .with("    </entity>")
        .with("    <entity name='void' sameEntity='true'>")
        .with("      <data source='nothing' />")
        .with("    </entity>")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1x");
    metamorph.startEntity("E");
    metamorph.literal("d1", "a");
    metamorph.literal("d2", "b");
    metamorph.endEntity();
    metamorph.startEntity("E");
    metamorph.literal("d1", "x");
    metamorph.endEntity();
    metamorph.startEntity("E");
    metamorph.literal("d1", "c");
    metamorph.literal("d2", "d");
    metamorph.endEntity();
    metamorph.literal("d", "c");
    metamorph.endRecord();
    metamorph.startRecord("2x");
    metamorph.literal("d", "c");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1x");
    ordered.verify(receiver).startEntity("uber");
    ordered.verify(receiver).startEntity("unter");
    ordered.verify(receiver).literal("l", "a");
    ordered.verify(receiver).literal("l", "b");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).startEntity("unter");
    ordered.verify(receiver).literal("l", "c");
    ordered.verify(receiver).literal("l", "d");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).literal("l", "c");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2x");
    ordered.verify(receiver).startEntity("uber");
    ordered.verify(receiver).literal("l", "c");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldSupportDeeplyNestedEnities() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity name='1'>")
        .with("    <data source='1' />")
        .with("    <entity name='2'>")
        .with("      <data source='2' />")
        .with("      <entity name='3'>")
        .with("        <entity name='4'>")
        .with("          <data source='4' />")
        .with("        </entity>")
        .with("      </entity>")
        .with("    </entity>")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1x");
    metamorph.literal("1", "a");
    metamorph.literal("2", "b");
    metamorph.literal("4", "c");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1x");
    ordered.verify(receiver).startEntity("1");
    ordered.verify(receiver).literal("1", "a");
    ordered.verify(receiver).startEntity("2");
    ordered.verify(receiver).literal("2", "b");
    ordered.verify(receiver).startEntity("3");
    ordered.verify(receiver).startEntity("4");
    ordered.verify(receiver).literal("4", "c");
    ordered.verify(receiver, times(4)).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldGetNameFromDataInEntityName() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity>")
        .with("    <entity-name>")
        .with("      <data source='data1'>")
        .with("        <compose prefix='entity:' />")
        .with("      </data>")
        .with("    </entity-name>")
        .with("    <data source='data1' name='l1' />")
        .with("    <data source='data2' name='l2' />")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1x");
    metamorph.literal("data1", "a1");
    metamorph.literal("data1", "a2");
    metamorph.literal("data2", "b");
    metamorph.endRecord();
    metamorph.startRecord("2x");
    metamorph.literal("data2", "c");
    metamorph.literal("data1", "d");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1x");
    ordered.verify(receiver).startEntity("entity:a2");
    ordered.verify(receiver).literal("l1", "a1");
    ordered.verify(receiver).literal("l1", "a2");
    ordered.verify(receiver).literal("l2", "b");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2x");
    ordered.verify(receiver).startEntity("entity:d");
    ordered.verify(receiver).literal("l2", "c");
    ordered.verify(receiver).literal("l1", "d");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldGetNameFromCollectInEntityName() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity>")
        .with("    <entity-name>")
        .with("      <combine name='' value='entity:${a},${b}'>")
        .with("        <data source='data1' name='a' />")
        .with("        <data source='data2' name='b' />")
        .with("      </combine>")
        .with("    </entity-name>")
        .with("    <data source='data1' name='l1' />")
        .with("    <data source='data2' name='l2' />")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1x");
    metamorph.literal("data1", "a1");
    metamorph.literal("data1", "a2");
    metamorph.literal("data2", "b");
    metamorph.endRecord();
    metamorph.startRecord("2x");
    metamorph.literal("data2", "c");
    metamorph.literal("data1", "d");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1x");
    ordered.verify(receiver).startEntity("entity:a2,b");
    ordered.verify(receiver).literal("l1", "a1");
    ordered.verify(receiver).literal("l1", "a2");
    ordered.verify(receiver).literal("l2", "b");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2x");
    ordered.verify(receiver).startEntity("entity:d,c");
    ordered.verify(receiver).literal("l2", "c");
    ordered.verify(receiver).literal("l1", "d");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldResetNameToNameAttribute() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity name='defaultName' flushWith='record'>")
        .with("    <entity-name>")
        .with("      <data source='data3' />")
        .with("    </entity-name>")
        .with("    <data source='data1' name='l1' />")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1x");
    metamorph.literal("data1", "a");
    metamorph.literal("data3", "dynamicName");
    metamorph.endRecord();
    metamorph.startRecord("2x");
    metamorph.literal("data1", "b");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1x");
    ordered.verify(receiver).startEntity("dynamicName");
    ordered.verify(receiver).literal("l1", "a");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2x");
    ordered.verify(receiver).startEntity("defaultName");
    ordered.verify(receiver).literal("l1", "b");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEmitEmptyStringIfEntityNameIsNotSet() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity>")
        .with("    <data source='in' name='out' />")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("in", "a");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).startEntity("");
    ordered.verify(receiver).literal("out", "a");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEmitEntityContentsAgainIfResetIsFalse() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity name='entity'>")
        .with("    <data source='lit1' />")
        .with("    <data source='lit2' />")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("lit1", "const");
    metamorph.literal("lit2", "1");
    metamorph.literal("lit2", "2");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).startEntity("entity");
    ordered.verify(receiver).literal("lit1", "const");
    ordered.verify(receiver).literal("lit2", "1");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).startEntity("entity");
    ordered.verify(receiver).literal("lit1", "const");
    ordered.verify(receiver).literal("lit2", "1");
    ordered.verify(receiver).literal("lit2", "2");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldNotEmitEntityContentsAgainIfResetIsFalse() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <entity name='entity' reset='true'>")
        .with("    <data source='lit1' />")
        .with("    <data source='lit2' />")
        .with("  </entity>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("lit1", "const");
    metamorph.literal("lit2", "1");
    metamorph.literal("lit2", "2");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).startEntity("entity");
    ordered.verify(receiver).literal("lit1", "const");
    ordered.verify(receiver).literal("lit2", "1");
    ordered.verify(receiver).endEntity();
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
