/*
 * Copyright 2023 Fabian Steeg, hbz
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

package org.metafacture.metafix;

import org.metafacture.formatting.ObjectTemplate;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultStreamPipe;
import org.metafacture.mangling.StreamFlattener;
import org.metafacture.triples.AbstractTripleSort.Compare;
import org.metafacture.triples.AbstractTripleSort.Order;
import org.metafacture.triples.StreamToTriples;
import org.metafacture.triples.TripleCount;
import org.metafacture.triples.TripleSort;

import java.io.FileNotFoundException;

/**
 * Superclass for Metafix-based analyzer modules based on triples (see {@link org.metafacture.framework.objects.Triple}).
 *
 * @author Fabian Steeg
 */
/* package-private */ class MetafixStreamAnalyzer extends DefaultStreamPipe<ObjectReceiver<String>> {

    private Metafix fix;
    private boolean count = true;
    private Compare countBy;
    private String countedTemplate;
    private String uncountedTemplate = "${s}";

    /* package-private */ MetafixStreamAnalyzer(final String fix, final Compare countBy,
            final String countedTemplate) {
        try {
            this.fix = new Metafix(fix);
            this.fix.setRepeatedFieldsToEntities(true);
        }
        catch (final FileNotFoundException e) {
            throw new MetafactureException(e);
        }
        this.countBy = countBy;
        this.countedTemplate = countedTemplate;
    }

    @Override
    protected void onSetReceiver() {
        fix
            .setReceiver(new StreamFlattener())
            .setReceiver(new StreamToTriples())
            .setReceiver(tripleCount())
            .setReceiver(tripleSort())
            .setReceiver(new ObjectTemplate<>(count ? countedTemplate : uncountedTemplate))
            .setReceiver(getReceiver());
    }

    private TripleCount tripleCount() {
        final TripleCount tripleCount = new TripleCount();
        tripleCount.setCountBy(countBy);
        return tripleCount;
    }

    private TripleSort tripleSort() {
        final TripleSort tripleSort = new TripleSort();
        tripleSort.setNumeric(count);
        tripleSort.setBy(Compare.OBJECT);
        tripleSort.setOrder(count ? Order.DECREASING : Order.INCREASING);
        return tripleSort;
    }

    @Override
    public void startRecord(final String identifier) {
        fix.startRecord(identifier);
    }

    @Override
    public void endRecord() {
        fix.endRecord();
    }

    @Override
    public void startEntity(final String name) {
        fix.startEntity(name);
    }

    @Override
    public void endEntity() {
        fix.endEntity();
    }

    @Override
    public void literal(final String name, final String value) {
        fix.literal(name, value);
    }

    @Override
    protected void onCloseStream() {
        fix.closeStream();
    }

    @Override
    protected void onResetStream() {
        fix.resetStream();
    }

    public void setCount(final boolean count) {
        this.count = count;
    }

    public boolean getCount() {
        return this.count;
    }

    /* package-private */ Metafix getFix() {
        return this.fix;
    }

    /* package-private */ void setFix(final Metafix fix) {
        this.fix = fix;
    }
}
