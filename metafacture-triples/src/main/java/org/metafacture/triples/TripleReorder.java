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

package org.metafacture.triples;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;
import org.metafacture.framework.objects.Triple;

/**
 * Shifts subjectTo predicateTo and objectTo around.
 *
 * @author Christoph Böhme
 *
 */
@Description("Shifts subjectTo predicateTo and objectTo around")
@In(Triple.class)
@Out(Triple.class)
@FluxCommand("reorder-triple")
public final class TripleReorder extends DefaultObjectPipe<Triple, ObjectReceiver<Triple>> {

    /**
     * Names of the elements in the triple
     */
    public enum TripleElement { SUBJECT, PREDICATE, OBJECT }
    // Do not change the item order because the process method
    // uses ordinal().

    private TripleElement subjectFrom = TripleElement.SUBJECT;
    private TripleElement predicateFrom = TripleElement.PREDICATE;
    private TripleElement objectFrom =  TripleElement.OBJECT;

    /**
     * Creates an instance of {@link TripleReorder}.
     */
    public TripleReorder() {
    }

    /**
     * Gets the TripleElement to shift the subject to.
     *
     * @return the {@link TripleElement}
     */
    public TripleElement getSubjectFrom() {
        return subjectFrom;
    }

    /**
     * Gets the TripleElement to shift the predicate to.
     *
     * @return the {@link TripleElement}
     */
    public TripleElement getPredicateFrom() {
        return predicateFrom;
    }

    /**
     * Gets the TripleElement to shift the object to.
     *
     * @return the {@link TripleElement}
     */
    public TripleElement getObjectFrom() {
        return objectFrom;
    }

    /**
     * Sets shifting the subject from the given TripleElement.
     *
     * @param subjectFrom the {@link TripleElement}
     */
    public void setSubjectFrom(final TripleElement subjectFrom) {
        this.subjectFrom = subjectFrom;
    }

    /**
     * Sets shifting the predicate from the given TripleElement.
     *
     * @param predicateFrom the {@link TripleElement}
     */
    public void setPredicateFrom(final TripleElement predicateFrom) {
        this.predicateFrom = predicateFrom;
    }

    /**
     * Sets shifting the object from the given TripleElement.
     *
     * @param objectFrom the {@link TripleElement}
     */
    public void setObjectFrom(final TripleElement objectFrom) {
        this.objectFrom = objectFrom;
    }

    @Override
    public void process(final Triple triple) {
        final String[] elements = {
                triple.getSubject(),
                triple.getPredicate(),
                triple.getObject()
        };

        getReceiver().process(new Triple(
                elements[subjectFrom.ordinal()],
                elements[predicateFrom.ordinal()],
                elements[objectFrom.ordinal()]
        ));
    }

}
