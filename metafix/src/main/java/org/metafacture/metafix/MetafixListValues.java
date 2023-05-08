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
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.Sender;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;
import org.metafacture.mangling.DuplicateObjectFilter;
import org.metafacture.triples.AbstractTripleSort.Compare;
import org.metafacture.triples.AbstractTripleSort.Order;
import org.metafacture.triples.StreamToTriples;
import org.metafacture.triples.TripleCount;
import org.metafacture.triples.TripleSort;

import java.io.FileNotFoundException;

/**
 * Provide a user-friendly way to list all values for a given path (see {@link MetafixListPaths}).
 *
 * @author Fabian Steeg
 */
@Description("Lists all values found for the given path. The paths can be found using fix-list-paths. Options: " +
        "count (output occurence frequency of each value, sorted by highest frequency first; default: true)")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("fix-list-values")
public class MetafixListValues extends DefaultStreamPipe<ObjectReceiver<String>> {

    private String path;
    private Metafix fix;
    private boolean count = true;

    public MetafixListValues(final String path) {
        this.path = path;
        try {
            fix = new Metafix("retain(\"" + path + "\")");
        }
        catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSetReceiver() {
        final StreamToTriples triples = fix
                .setReceiver(new StreamToTriples());
        (count ? counted(triples) : unique(triples))
                .setReceiver(getReceiver());
    }

    private Sender<ObjectReceiver<String>> counted(final StreamToTriples triples) {
        return triples
                .setReceiver(tripleCount())
                .setReceiver(tripleSort())
                .setReceiver(new ObjectTemplate<>("${o}\t ${s}"));
    }

    private Sender<ObjectReceiver<String>> unique(final StreamToTriples triples) {
        return triples
                .setReceiver(new ObjectTemplate<>("${o}"))
                .setReceiver(new DuplicateObjectFilter<>());
    }

    private TripleCount tripleCount() {
        final TripleCount tripleCount = new TripleCount();
        tripleCount.setCountBy(Compare.OBJECT);
        return tripleCount;
    }

    private TripleSort tripleSort() {
        final TripleSort tripleSort = new TripleSort();
        tripleSort.setNumeric(true);
        tripleSort.setBy(Compare.OBJECT);
        tripleSort.setOrder(Order.DECREASING);
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

    public void setPath(final String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }
}
