/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.statistics;

import static org.junit.Assert.assertEquals;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.helpers.DefaultObjectReceiver;
import org.culturegraph.mf.framework.objects.Triple;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for class {@link CooccurrenceMetricCalculator}.
 *
 * @author Markus Michael Geipel
 *
 */
public final class CooccurrenceMetricCalculatorTest {

	private static final double DELTA = 0.01;
	private static final int TOTAL = 1000;
	private static final int COUNT_A = 100;
	private static final int COUNT_B = 50;
	private static final int COUNT_A_AND_B = 10;

	private static final double X2 = 7.1359;
	private static final double F = 0.1333;
	private static final double RECALL = 0.199;
	private static final double PRECISSION = 0.099;
	private static final double JACCARD = 0.0714;

	@Test
	public void testX2() {
		assertEquals(X2, CooccurrenceMetricCalculator.Metric.X2.calculate(COUNT_A, COUNT_B, COUNT_A_AND_B, TOTAL),
				DELTA);
	}

	@Test
	public void testF() {
		assertEquals(F, CooccurrenceMetricCalculator.Metric.F.calculate(COUNT_A, COUNT_B, COUNT_A_AND_B, TOTAL), DELTA);
	}

	@Test
	public void testPrecission() {
		assertEquals(PRECISSION,
				CooccurrenceMetricCalculator.Metric.PRECISSION.calculate(COUNT_A, COUNT_B, COUNT_A_AND_B, TOTAL), DELTA);
	}

	@Test
	public void testRecall() {
		assertEquals(RECALL,
				CooccurrenceMetricCalculator.Metric.RECALL.calculate(COUNT_A, COUNT_B, COUNT_A_AND_B, TOTAL), DELTA);
	}

	@Test
	public void testJaccard() {
		assertEquals(JACCARD,
				CooccurrenceMetricCalculator.Metric.JACCARD.calculate(COUNT_A, COUNT_B, COUNT_A_AND_B, TOTAL), DELTA);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAll() {
		final CooccurrenceMetricCalculator calculator = new CooccurrenceMetricCalculator("X2, F");
		final ObjectReceiver<Triple> receiver = Mockito.mock(ObjectReceiver.class);
		calculator.setReceiver(receiver);
		calculator.process(new Triple("1:", "", Integer.toString(TOTAL)));
		calculator.process(new Triple("1:A", "", Integer.toString(COUNT_A)));
		calculator.process(new Triple("1:B", "", Integer.toString(COUNT_B)));
		calculator.process(new Triple("2:A&B", "", Integer.toString(COUNT_A_AND_B)));

		Mockito.verify(receiver).process(new Triple("A&B", CooccurrenceMetricCalculator.Metric.X2.toString(), Double.toString(CooccurrenceMetricCalculator.Metric.X2.calculate(COUNT_A, COUNT_B, COUNT_A_AND_B, TOTAL))));
	}


	@Test(expected=IllegalArgumentException.class)
	public void testIllegalArgument() {
		final CooccurrenceMetricCalculator calculator = new CooccurrenceMetricCalculator("X2");
		calculator.setReceiver(new DefaultObjectReceiver<Triple>());
		calculator.process(new Triple("2:x&x", "", Integer.toString(COUNT_A_AND_B)));
		calculator.process(new Triple("1:x", "", Integer.toString(COUNT_B)));


	}
}
