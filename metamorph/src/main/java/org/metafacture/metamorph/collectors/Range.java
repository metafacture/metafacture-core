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

package org.metafacture.metamorph.collectors;

import org.metafacture.metamorph.api.NamedValueSource;
import org.metafacture.metamorph.api.helpers.AbstractFlushingCollect;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Corresponds to the <code>&lt;range&gt;</code> tag.
 *
 * @author Christoph Böhme
 */
public final class Range extends AbstractFlushingCollect {

    private final SortedSet<Integer> values = new TreeSet<Integer>(new IncrementDependingComparator());

    private int increment;
    private Integer first;

    /**
     * Creates an instance of {@link Range}.
     */
    public Range() {
    }

    /**
     * Gets the incrment.
     *
     * @return the increment
     */
    public int getIncrement() {
        return increment;
    }

    /**
     * Sets the incremenet.
     *
     * @param increment the increment
     */
    public void setIncrement(final int increment) {
        this.increment = increment;
    }

    @Override
    protected void emit() {
        for (final Integer i: values) {
            getNamedValueReceiver().receive(getName(), i.toString(), this, getRecordCount(), getEntityCount());
        }
    }

    @Override
    protected boolean isComplete() {
        return false;
    }

    @Override
    protected void receive(final String name, final String value, final NamedValueSource source) {
        if (first == null) {
            first = Integer.valueOf(value);
        }
        else {
            final int last = Integer.valueOf(value).intValue();
            for (int i = first.intValue(); (increment > 0 && i <= last) || (increment < 0 && i >= last); i += increment) {
                values.add(Integer.valueOf(i));
            }
            first = null;
        }
    }

    @Override
    protected void clear() {
        values.clear();
        first = null;
    }

    /**
     * A comparator which defines the sort order of the values in the range
     * depending on the increment.
     */
    private class IncrementDependingComparator implements Comparator<Integer> {

        IncrementDependingComparator() {
        }

        @Override
        public int compare(final Integer o1, final Integer o2) {
            return Integer.signum(increment) * (o1 - o2);
        }

    }

}
