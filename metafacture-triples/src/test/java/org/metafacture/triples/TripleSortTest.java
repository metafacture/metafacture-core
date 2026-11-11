/*
 * Copyright 2021 hbz NRW
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

package org.metafacture.triples;

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.objects.Triple;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TripleSortTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ObjectReceiver<Triple> receiver;

    public TripleSortTest() {
    }

    @Test
    public void shouldSortByIncreasingSubject() {
        assertSort(
                t -> {
                },
                "s0 p1 o2",
                "s2 p1 o0",
                "s0 p1 o1",
                "s1 p0 o2",
                "s0 p2 o1",
                //
                "s0 p1 o2",
                "s0 p1 o1",
                "s0 p2 o1",
                "s1 p0 o2",
                "s2 p1 o0"
        );
    }

    @Test
    public void shouldSortByDecreasingSubject() {
        assertSort(
                t -> {
                    t.setOrder(AbstractTripleSort.Order.DECREASING);
                },
                "s0 p1 o2",
                "s2 p1 o0",
                "s0 p1 o1",
                "s1 p0 o2",
                "s0 p2 o1",
                //
                "s2 p1 o0",
                "s1 p0 o2",
                "s0 p1 o2",
                "s0 p1 o1",
                "s0 p2 o1"
        );
    }

    @Test
    public void shouldSortByIncreasingPredicate() {
        assertSort(
                t -> {
                    t.setBy(AbstractTripleSort.Compare.PREDICATE);
                },
                "s0 p1 o2",
                "s2 p1 o0",
                "s0 p1 o1",
                "s1 p0 o2",
                "s0 p2 o1",
                //
                "s1 p0 o2",
                "s0 p1 o2",
                "s2 p1 o0",
                "s0 p1 o1",
                "s0 p2 o1"
        );
    }

    @Test
    public void shouldSortByDecreasingPredicate() {
        assertSort(
                t -> {
                    t.setBy(AbstractTripleSort.Compare.PREDICATE);
                    t.setOrder(AbstractTripleSort.Order.DECREASING);
                },
                "s0 p1 o2",
                "s2 p1 o0",
                "s0 p1 o1",
                "s1 p0 o2",
                "s0 p2 o1",
                //
                "s0 p2 o1",
                "s0 p1 o2",
                "s2 p1 o0",
                "s0 p1 o1",
                "s1 p0 o2"
        );
    }

    @Test
    public void shouldSortByIncreasingObject() {
        assertSort(
                t -> {
                    t.setBy(AbstractTripleSort.Compare.OBJECT);
                },
                "s0 p1 o2",
                "s2 p1 o0",
                "s0 p1 o1",
                "s1 p0 o2",
                "s0 p2 o1",
                //
                "s2 p1 o0",
                "s0 p1 o1",
                "s0 p2 o1",
                "s0 p1 o2",
                "s1 p0 o2"
        );
    }

    @Test
    public void shouldSortByDecreasingObject() {
        assertSort(
                t -> {
                    t.setBy(AbstractTripleSort.Compare.OBJECT);
                    t.setOrder(AbstractTripleSort.Order.DECREASING);
                },
                "s0 p1 o2",
                "s2 p1 o0",
                "s0 p1 o1",
                "s1 p0 o2",
                "s0 p2 o1",
                //
                "s0 p1 o2",
                "s1 p0 o2",
                "s0 p1 o1",
                "s0 p2 o1",
                "s2 p1 o0"
        );
    }

    @Test
    public void shouldSortByIncreasingTriple() {
        assertSort(
                t -> {
                    t.setBy(AbstractTripleSort.Compare.ALL);
                },
                "s0 p1 o2",
                "s2 p1 o0",
                "s0 p1 o1",
                "s1 p0 o2",
                "s0 p2 o1",
                //
                "s0 p1 o1",
                "s0 p1 o2",
                "s0 p2 o1",
                "s1 p0 o2",
                "s2 p1 o0"
        );
    }

    @Test
    public void shouldSortByDecreasingTriple() {
        assertSort(
                t -> {
                    t.setBy(AbstractTripleSort.Compare.ALL);
                    t.setOrder(AbstractTripleSort.Order.DECREASING);
                },
                "s0 p1 o2",
                "s2 p1 o0",
                "s0 p1 o1",
                "s1 p0 o2",
                "s0 p2 o1",
                //
                "s2 p1 o0",
                "s1 p0 o2",
                "s0 p2 o1",
                "s0 p1 o2",
                "s0 p1 o1"
        );
    }

    @Test
    public void shouldNotSortNumerically() {
        assertSort(
                t -> {
                },
                "10  p1 o2",
                "2   p1 o0",
                "0   p1 o1",
                "101 p0 o2",
                "11  p2 o1",
                //
                "0   p1 o1",
                "10  p1 o2",
                "101 p0 o2",
                "11  p2 o1",
                "2   p1 o0"
        );
    }

    @Test
    public void issue380_shouldOptionallySortNumericallyByIncreasingSubject() {
        assertSort(
                t -> {
                    t.setNumeric(true);
                },
                "10  p1 o2",
                "2   p1 o0",
                "0   p1 o1",
                "101 p0 o2",
                "11  p2 o1",
                //
                "0   p1 o1",
                "2   p1 o0",
                "10  p1 o2",
                "11  p2 o1",
                "101 p0 o2"
        );
    }

    @Test
    public void issue380_shouldOptionallySortNumericallyByDecreasingSubject() {
        assertSort(
                t -> {
                    t.setNumeric(true);
                    t.setOrder(AbstractTripleSort.Order.DECREASING);
                },
                "10  p1 o2",
                "2   p1 o0",
                "0   p1 o1",
                "101 p0 o2",
                "11  p2 o1",
                //
                "101 p0 o2",
                "11  p2 o1",
                "10  p1 o2",
                "2   p1 o0",
                "0   p1 o1"
        );
    }

    @Test(expected = NumberFormatException.class)
    public void shouldFailToSortNumericallyWithInvalidNumber() {
        assertSort(
                t -> {
                    t.setNumeric(true);
                },
                "10  p1 o2",
                "2   p1 o0",
                "0   p1 o1",
                "1x1 p0 o2",
                "11  p2 o1",
                //
                "",
                "",
                "",
                "",
                ""
        );
    }

    @Test
    public void shouldNotSortNumericallyByTriple() {
        assertSort(
                t -> {
                    t.setNumeric(true);
                    t.setBy(AbstractTripleSort.Compare.ALL);
                },
                "10  p1 o2",
                "2   p1 o0",
                "0   p1 o1",
                "101 p0 o2",
                "11  p2 o1",
                //
                "0   p1 o1",
                "10  p1 o2",
                "101 p0 o2",
                "11  p2 o1",
                "2   p1 o0"
        );
    }

    public void assertSort(final Consumer<TripleSort> consumer, final String... triples) {
        final BiConsumer<Integer, Consumer<Triple>> processor = (i, c) -> {
            final int j = triples.length / 2;

            Arrays.stream(triples, i * j, (i + 1) * j).map(s -> {
                final String[] t = s.split("\\s+");
                return new Triple(t[0], t[1], t[2]);
            }).forEach(c);
        };

        final InOrder ordered = Mockito.inOrder(receiver);

        final TripleSort tripleSort = new TripleSort();
        tripleSort.setReceiver(receiver);
        consumer.accept(tripleSort);

        processor.accept(0, tripleSort::process);
        tripleSort.closeStream();

        try {
            processor.accept(1, t -> ordered.verify(receiver).process(t));
            ordered.verify(receiver).closeStream();

            ordered.verifyNoMoreInteractions();
            Mockito.verifyNoMoreInteractions(receiver);
        }
        catch (final MockitoAssertionError e) {
            System.out.println(Mockito.mockingDetails(receiver).printInvocations());
            throw e;
        }
    }

}
