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
 * Tests for class {@link Tuples}.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class TuplesTest {

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void shouldEmitTwoandThreeTuples() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <tuples name='product'>")
        .with("    <data source='1' />")
        .with("    <data source='3' />")
        .with("    <data source='2' />")
        .with("  </tuples>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("1", "a");
    metamorph.literal("1", "b");
    metamorph.literal("2", "A");
    metamorph.literal("2", "B");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("3", "X");
    metamorph.literal("1", "c");
    metamorph.literal("1", "d");
    metamorph.literal("2", "C");
    metamorph.literal("3", "Y");
    metamorph.literal("2", "D");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("product", "aA");
    ordered.verify(receiver).literal("product", "bA");
    ordered.verify(receiver).literal("product", "aB");
    ordered.verify(receiver).literal("product", "bB");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).literal("product", "cCX");
    ordered.verify(receiver).literal("product", "dCX");
    ordered.verify(receiver).literal("product", "cDX");
    ordered.verify(receiver).literal("product", "dDX");
    ordered.verify(receiver).literal("product", "cCY");
    ordered.verify(receiver).literal("product", "dCY");
    ordered.verify(receiver).literal("product", "cDY");
    ordered.verify(receiver).literal("product", "dDY");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldOnlyEmitTriplesWithMoreThanMinNValues() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <tuples name='product' minN='3'>")
        .with("    <data source='1' />")
        .with("    <data source='3' />")
        .with("    <data source='2' />")
        .with("  </tuples>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("1", "a");
    metamorph.literal("1", "b");
    metamorph.literal("2", "A");
    metamorph.literal("2", "B");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.literal("3", "X");
    metamorph.literal("1", "c");
    metamorph.literal("1", "d");
    metamorph.literal("2", "C");
    metamorph.literal("3", "Y");
    metamorph.literal("2", "D");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).literal("product", "cCX");
    ordered.verify(receiver).literal("product", "dCX");
    ordered.verify(receiver).literal("product", "cDX");
    ordered.verify(receiver).literal("product", "dDX");
    ordered.verify(receiver).literal("product", "cCY");
    ordered.verify(receiver).literal("product", "dCY");
    ordered.verify(receiver).literal("product", "cDY");
    ordered.verify(receiver).literal("product", "dDY");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void shouldEmitTuplesWithMinNIfNotAllStatementsFired() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <tuples name='product' minN='1'>")
        .with("    <data source='1' />")
        .with("    <data source='3' />")
        .with("    <data source='2' />")
        .with("  </tuples>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("1", "a");
    metamorph.literal("1", "b");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("product", "a");
    ordered.verify(receiver).literal("product", "b");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

}
