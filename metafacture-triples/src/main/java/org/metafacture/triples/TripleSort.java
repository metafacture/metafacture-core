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

/**
 * Sorts triples
 * @author markus geipel
 *
 */
@Description("Sorts triples. Several options can be combined, e.g. `by=\"object\",numeric=\"true\",order=\"decreasing\"` will numerically sort the Object of the triples in decreasing order (given that all Objects are indeed of numeric type).")
@In(Triple.class)
@Out(Triple.class)
@FluxCommand("sort-triples")
public final class TripleSort extends AbstractTripleSort {

    /**
     * Creates an instance of {@link TripleSort}.
     */
    public TripleSort() {
    }

    @Override
    protected void sortedTriple(final Triple triple) {
        getReceiver().process(triple);
    }

    /**
     * Sets if the order should be done by subject, predicate or object.
     *
     * @param compare the {@link AbstractTripleSort.Compare}
     */
    public void setBy(final Compare compare) {
        setCompare(compare);
    }

    /**
     * Sets increasing or decreasing order.
     *
     * @param order the {@link AbstractTripleSort.Order}.
     */
    public void setOrder(final Order order) {
        setSortOrder(order);
    }

    /**
     * Flags if sort should be numeric.
     *
     * @param numeric true if sort should be numeric
     */
    public void setNumeric(final boolean numeric) {
        setSortNumeric(numeric);
    }

}
