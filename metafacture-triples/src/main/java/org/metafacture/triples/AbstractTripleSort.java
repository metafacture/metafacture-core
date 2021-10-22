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

package org.metafacture.triples;

import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultObjectPipe;
import org.metafacture.framework.objects.Triple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;

/**
 * @author markus geipel
 *
 */
public abstract class AbstractTripleSort extends DefaultObjectPipe<Triple, ObjectReceiver<Triple>> implements MemoryWarningSystem.Listener {

    /**
     * Specifies the comparator. Can be one of {@link #SUBJECT}, {@link #PREDICATE},
     * {@link #OBJECT}, {@link #ALL}.
     */
    public enum Compare {
        SUBJECT, PREDICATE, OBJECT, ALL
    }

    /**
     * Specifies the sort order.Can be one of {@link #INCREASING},
     * {@link #DECREASING}.
     */
    public enum Order {
        INCREASING {
            @Override
            public int order(final int indicator) {
                return indicator;
            }
        },
        DECREASING {
            @Override
            public int order(final int indicator) {
                return -indicator;
            }
        };
        public abstract int order(int indicator);
    }

    private final List<Triple> buffer = new ArrayList<>();
    private final List<File> tempFiles = new ArrayList<>();
    private Compare compare = Compare.SUBJECT;
    private Order order = Order.INCREASING;
    private boolean numeric;
    private volatile boolean memoryLow;

    /**
     * Constructs an AbstractTripleSort. Calls {@link MemoryWarningSystem}.
     */
    protected AbstractTripleSort() {
        MemoryWarningSystem.addListener(this);
    }

    @Override
    public final void memoryLow(final long usedMemory, final long maxMemory) {
        memoryLow = true;
    }

    protected final void setCompare(final Compare compare) {
        this.compare = compare;
    }

    protected final Compare getCompare() {
        return compare;
    }

    protected final void setSortOrder(final Order newOrder) {
        order = newOrder;
    }

    protected final void setSortNumeric(final boolean newNumeric) {
        numeric = newNumeric;
    }

    @Override
    public final void process(final Triple namedValue) {
        if (memoryLow) {
            try {
                if (!buffer.isEmpty()) {
                    nextBatch();
                }
            }
            catch (final IOException e) {
                throw new MetafactureException("Error writing to temp file after sorting", e);
            }
            finally {
                memoryLow = false;
            }
        }
        buffer.add(namedValue);
    }

    private void nextBatch() throws IOException {
        Collections.sort(buffer, createComparator());
        final File tempFile = File.createTempFile("sort", "namedValues", null);
        tempFile.deleteOnExit();

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            for (final Triple triple : buffer) {
                triple.write(out);
            }
        }

        buffer.clear();
        tempFiles.add(tempFile);
    }

    @Override
    public final void onCloseStream() {
        if (tempFiles.isEmpty()) {
            Collections.sort(buffer, createComparator());

            for (final Triple triple : buffer) {
                sortedTriple(triple);
            }

            onFinished();
        }
        else {
            final Comparator<Triple> comparator = createComparator();
            final PriorityQueue<SortedTripleFileFacade> queue = new PriorityQueue<>(11, (o1, o2) -> comparator.compare(o1.peek(), o2.peek()));

            try {
                nextBatch();

                for (final File file : tempFiles) {
                    queue.add(new SortedTripleFileFacade(file));
                }

                while (queue.size() > 0) {
                    final SortedTripleFileFacade sortedFileFacade = queue.poll();
                    final Triple triple = sortedFileFacade.pop();
                    sortedTriple(triple);
                    if (sortedFileFacade.isEmpty()) {
                        sortedFileFacade.close();
                    }
                    else {
                        queue.add(sortedFileFacade);
                    }
                }

                onFinished();
            }
            catch (final IOException e) {
                throw new MetafactureException("Error merging temp files", e);
            }
            finally {
                for (final SortedTripleFileFacade sortedFileFacade : queue) {
                    sortedFileFacade.close();
                }
            }
        }

        MemoryWarningSystem.removeListener(this);
    }

    protected void onFinished() {
        // nothing to do
    }

    protected abstract void sortedTriple(Triple namedValue);

    public final Comparator<Triple> createComparator() {
        return createComparator(compare, order, numeric);
    }

    public static Comparator<Triple> createComparator(final Compare compare, final Order order) {
        return createComparator(compare, order, false);
    }

    /**
     * Creates a Comparator.
     *
     * @param compare one of {@link #Compare}
     * @param order   the {@link #Order}
     * @param numeric "true" if comparison should be numeric. "false" if comparison
     *                should be alphanumeric. Defaults to "false".
     * @return a Comparator of type Triple
     */
    private static Comparator<Triple> createComparator(final Compare compare, final Order order, final boolean numeric) {
        final Function<Triple, String> tripleFunction;
        switch (compare) {
            case ALL:
                return (o1, o2) -> order.order(o1.compareTo(o2));
            case OBJECT:
                tripleFunction = Triple::getObject;
                break;
            case SUBJECT:
                tripleFunction = Triple::getSubject;
                break;
            case PREDICATE:
            default:
                tripleFunction = Triple::getPredicate;
                break;
        }

        final Function<Triple, Integer> numericFunction = tripleFunction.andThen(Integer::valueOf);
        return numeric ?
            (o1, o2) -> order.order(numericFunction.apply(o1).compareTo(numericFunction.apply(o2))) :
            (o1, o2) -> order.order(tripleFunction.apply(o1).compareTo(tripleFunction.apply(o2)));
    }

    @Override
    public final void onResetStream() {
        buffer.clear();

        for (final File file : tempFiles) {
            if (file.exists()) {
                file.delete();
            }
        }

        tempFiles.clear();
    }

}
