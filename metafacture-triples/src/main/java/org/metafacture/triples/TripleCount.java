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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.objects.Triple;

import java.util.Comparator;

/**
 * Counts triples.
 *
 * @author markus geipel
 *
 */
@Description("Counts triples")
@In(Triple.class)
@Out(Triple.class)
@FluxCommand("count-triples")
public final class TripleCount extends AbstractTripleSort {

    public static final String DEFAULT_COUNT_PREDICATE = "count";

    @Deprecated/*(since="5.3", forRemoval=true)*/
    public static final String DEFAULT_COUNTP_REDICATE = DEFAULT_COUNT_PREDICATE;

    private static final Triple INIT = new Triple("", "", "");

    private Triple current = INIT;
    private int count;
    private String countPredicate = DEFAULT_COUNT_PREDICATE;
    private Comparator<Triple> comparator;

    /**
     * Creates an instance of {@link TripleCount}.
     */
    public TripleCount() {
    }

    @Override
    protected void sortedTriple(final Triple triple) {
        if (current == INIT) {
            current = triple;
            comparator = createComparator();
        }

        if (comparator.compare(current, triple) == 0) {
            ++count;
        }
        else {
            writeResult();
            current = triple;
            count = 1;
        }
    }

    /**
     * Flags whether predicates should be counted.
     *
     * @param countPredicate true if predicates should be counted
     */
    public void setCountPredicate(final String countPredicate) {
        this.countPredicate = countPredicate;
    }

    @Override
    protected void onFinished() {
        writeResult();
    }

    private void writeResult() {
        final Compare compareBy = getCompare();
        switch (compareBy) {
            case ALL:
                getReceiver().process(new Triple(current.toString(), countPredicate, String.valueOf(count)));
                break;
            case OBJECT:
                getReceiver().process(new Triple(current.getObject(), countPredicate, String.valueOf(count)));
                break;
            case PREDICATE:
                getReceiver().process(new Triple(current.getPredicate(), countPredicate, String.valueOf(count)));
                break;
            case SUBJECT:
            default:
                getReceiver().process(new Triple(current.getSubject(), countPredicate, String.valueOf(count)));
                break;
        }
    }

    /**
     * Compare triples by subject, predicate or object.
     *
     * @param countBy the {@link AbstractTripleSort.Compare} to sort by
     */
    public void setCountBy(final Compare countBy) {
        setCompare(countBy);
    }
}
