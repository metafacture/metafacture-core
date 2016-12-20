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
package org.culturegraph.mf.metamorph.functions;

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
 * Tests for various Metamorph functions.
 *
 * @author Markus Geipel (metamorph-test xml)
 * @author Christoph Böhme (conversion to Java)
 */
public final class TestVariousFunctions {

  // TODO: This class need to be split into separate classes for each function!

  @Rule
  public final MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private StreamReceiver receiver;

  private Metamorph metamorph;

  @Test
  public void testRegexpFunction() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='data' name='island'>")
        .with("    <regexp match='(\\w*) island' format='${1}' />")
        .with("  </data>")
        .with("  <data source='data' name='year'>")
        .with("    <regexp match='\\d\\d\\d\\d' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data", "Aloha!");
    metamorph.literal("data", "Oahu island, Hawaii island, Maui island");
    metamorph.literal("data", "year 1960!");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("island", "Oahu");
    ordered.verify(receiver).literal("island", "Hawaii");
    ordered.verify(receiver).literal("island", "Maui");
    ordered.verify(receiver).literal("year", "1960");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void testIsbnFunction() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='isbn' name='withError'>")
        .with("    <isbn to='isbn13' errorString='error' />")
        .with("  </data>")
        .with("  <data source='isbn' name='withoutError'>")
        .with("    <isbn to='isbn13' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("isbn", "123 invalid");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("withError", "error");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void testSplitFunction() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='data' name='island'>")
        .with("    <split delimiter=',' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data", "Oahu,Hawaii,Maui");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("island", "Oahu");
    ordered.verify(receiver).literal("island", "Hawaii");
    ordered.verify(receiver).literal("island", "Maui");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void testSubstringFunction() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='a'>")
        .with("    <substring start='3' end='5' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("a", "012345");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("a", "34");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void testConstantFunction() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='data'>")
        .with("    <constant value='Hawaii' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data", "Aloha");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("data", "Hawaii");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void testSetReplaceFunction() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='data'>")
        .with("    <setreplace>")
        .with("      <entry name='dt.' value='deutsch' />")
        .with("      <entry name='frz.' value='französich' />")
        .with("      <entry name='eng.' value='englisch' />")
        .with("    </setreplace>")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data", "dt., frz. und eng.");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("data", "deutsch, französich und englisch");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void testCaseFunction() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='data'>")
        .with("    <case to='upper' />")
        .with("  </data>")
        .with("  <data source='data'>")
        .with("    <case to='lower' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data", "Aloha");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("data", "ALOHA");
    ordered.verify(receiver).literal("data", "aloha");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void testEqualsFunction() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <data source='data' name='data1'>")
        .with("    <equals string='Aloha' />")
        .with("  </data>")
        .with("  <data source='data' name='data2'>")
        .with("    <not-equals string='Aloha' />")
        .with("  </data>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("1");
    metamorph.literal("data", "Aloha");
    metamorph.literal("data", "Hawaii");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("data1", "Aloha");
    ordered.verify(receiver).literal("data2", "Hawaii");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

  @Test
  public void testBufferFunction() {
    metamorph = InlineMorph.in(this)
        .with("<rules>")
        .with("  <combine name='greeting' value='${greet} ${island}' reset='false'>")
        .with("    <data source='d1' name='greet' />")
        .with("    <data source='d2' name='island'>")
        .with("      <buffer />")
        .with("    </data>")
        .with("  </combine>")
        .with("</rules>")
        .createConnectedTo(receiver);

    metamorph.startRecord("0");
    metamorph.literal("d1", "Aloha");
    metamorph.endRecord();
    metamorph.startRecord("1");
    metamorph.literal("d2", "Hawaii");
    metamorph.literal("d2", "Oahu");
    metamorph.literal("d1", "Aloha");
    metamorph.endRecord();
    metamorph.startRecord("2");
    metamorph.endRecord();
    metamorph.startRecord("3");
    metamorph.literal("d1", "Aloha");
    metamorph.endRecord();
    metamorph.startRecord("4");
    metamorph.literal("d2", "to all");
    metamorph.literal("d1", "Aloha");
    metamorph.endRecord();

    final InOrder ordered = inOrder(receiver);
    ordered.verify(receiver).startRecord("0");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("1");
    ordered.verify(receiver).literal("greeting", "Aloha Hawaii");
    ordered.verify(receiver).literal("greeting", "Aloha Oahu");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("2");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("3");
    ordered.verify(receiver).endRecord();
    ordered.verify(receiver).startRecord("4");
    ordered.verify(receiver).literal("greeting", "Aloha to all");
    ordered.verify(receiver).endRecord();
    ordered.verifyNoMoreInteractions();
  }

	@Test
	public void testOccurrenceFunction() {
		metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <data source='data' name='l2'>")
				.with("    <occurrence only='lessThan 2' />")
				.with("  </data>")
				.with("  <data source='data' name='2'>")
				.with("    <occurrence only='2' />")
				.with("  </data>")
				.with("  <data source='data' name='g2'>")
				.with("    <occurrence only='moreThan 2' />")
				.with("  </data>")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.literal("data", "1");
		metamorph.literal("data", "2");
		metamorph.literal("data", "3");
		metamorph.endRecord();
		metamorph.startRecord("2");
		metamorph.literal("data", "1");
		metamorph.literal("data", "2");
		metamorph.literal("data", "3");
		metamorph.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("l2", "1");
		ordered.verify(receiver).literal("2", "2");
		ordered.verify(receiver).literal("g2", "3");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord("2");
		ordered.verify(receiver).literal("l2", "1");
		ordered.verify(receiver).literal("2", "2");
		ordered.verify(receiver).literal("g2", "3");
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void testOccurrenceFunctionWithSameEntity() {
		metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <data source='e.data' name='l2'>")
				.with("    <occurrence only='lessThan 2' sameEntity='true' />")
				.with("  </data>")
				.with("  <data source='e.data' name='2'>")
				.with("    <occurrence only='2' sameEntity='true' />")
				.with("  </data>")
				.with("  <data source='e.data' name='g2'>")
				.with("    <occurrence only='moreThan 2' sameEntity='true' />")
				.with("  </data>")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.startEntity("e");
		metamorph.literal("data", "1");
		metamorph.literal("data", "2");
		metamorph.literal("data", "3");
		metamorph.endEntity();
		metamorph.startEntity("e");
		metamorph.literal("data", "1");
		metamorph.literal("data", "2");
		metamorph.literal("data", "3");
		metamorph.endEntity();
		metamorph.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("l2", "1");
		ordered.verify(receiver).literal("2", "2");
		ordered.verify(receiver).literal("g2", "3");
		ordered.verify(receiver).literal("l2", "1");
		ordered.verify(receiver).literal("2", "2");
		ordered.verify(receiver).literal("g2", "3");
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void testOccurrenceFunctionWithSameEntityInNestedEntitiesShouldChangeWithInnerEntities() {
		metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <data source='o.i.data' name='l2'>")
				.with("    <occurrence only='lessThan 2' sameEntity='true' />")
				.with("  </data>")
				.with("  <data source='o.i.data' name='2'>")
				.with("    <occurrence only='2' sameEntity='true' />")
				.with("  </data>")
				.with("  <data source='o.i.data' name='g2'>")
				.with("    <occurrence only='moreThan 2' sameEntity='true' />")
				.with("  </data>")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.startEntity("o");
		metamorph.startEntity("i");
		metamorph.literal("data", "1");
		metamorph.literal("data", "2");
		metamorph.literal("data", "3");
		metamorph.endEntity();
		metamorph.startEntity("i");
		metamorph.literal("data", "1");
		metamorph.literal("data", "2");
		metamorph.literal("data", "3");
		metamorph.endEntity();
		metamorph.endEntity();
		metamorph.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("l2", "1");
		ordered.verify(receiver).literal("2", "2");
		ordered.verify(receiver).literal("g2", "3");
		ordered.verify(receiver).literal("l2", "1");
		ordered.verify(receiver).literal("2", "2");
		ordered.verify(receiver).literal("g2", "3");
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void testCountFunction() {
		metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <data source='data' name='count'>")
				.with("    <count />")
				.with("  </data>")
				.with("  <choose flushWith='record'>")
				.with("    <data source='datax' name='count'>")
				.with("      <count />")
				.with("    </data>")
				.with("  </choose>")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("0");
		metamorph.literal("datax", "1");
		metamorph.literal("datax", "2");
		metamorph.endRecord();
		metamorph.startRecord("1");
		metamorph.literal("data", "1");
		metamorph.literal("data", "2");
		metamorph.literal("data", "3");
		metamorph.endRecord();
		metamorph.startRecord("2");
		metamorph.literal("data", "1");
		metamorph.literal("data", "2");
		metamorph.endRecord();
		metamorph.startRecord("3");
		metamorph.literal("datax", "1");
		metamorph.literal("datax", "2");
		metamorph.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("0");
		ordered.verify(receiver).literal("count", "2");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("count", "1");
		ordered.verify(receiver).literal("count", "2");
		ordered.verify(receiver).literal("count", "3");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord("2");
		ordered.verify(receiver).literal("count", "1");
		ordered.verify(receiver).literal("count", "2");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord("3");
		ordered.verify(receiver).literal("count", "2");
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

	@Test
	public void testNestedCountFunction() {
		metamorph = InlineMorph.in(this)
				.with("<rules>")
				.with("  <combine name='count' value='${count}' flushWith='record'>")
				.with("    <data source='data' name='count'>")
				.with("      <count />")
				.with("    </data>")
				.with("    <data source='fantasy' />")
				.with("  </combine>")
				.with("</rules>")
				.createConnectedTo(receiver);

		metamorph.startRecord("1");
		metamorph.literal("data", "1");
		metamorph.literal("data", "2");
		metamorph.literal("data", "3");
		metamorph.endRecord();
		metamorph.startRecord("2");
		metamorph.literal("data", "1");
		metamorph.literal("data", "2");
		metamorph.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("1");
		ordered.verify(receiver).literal("count", "3");
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord("2");
		ordered.verify(receiver).literal("count", "2");
		ordered.verify(receiver).endRecord();
		ordered.verifyNoMoreInteractions();
	}

}
