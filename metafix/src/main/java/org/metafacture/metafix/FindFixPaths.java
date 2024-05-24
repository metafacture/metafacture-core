/*
 * Copyright 2024 Tobias Bülte, hbz
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
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;
import org.metafacture.mangling.StreamFlattener;
import org.metafacture.triples.StreamToTriples;
import org.metafacture.triples.TripleFilter;

import java.io.IOException;

/**
 * Provide a user-friendly way to finds all paths that have values that match
 * the given pattern.
 *
 * @author Tobias Bülte
 */
@Description("Finds all paths that have values that match the given pattern. Allows for regex. These paths can be used in a Fix to address fields.")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("find-fix-paths")

public class FindFixPaths extends DefaultStreamPipe<ObjectReceiver<String>> {
    private final Metafix fix;
    private String objectPattern;

    public FindFixPaths(final String objectPattern) {
        this.objectPattern = objectPattern;
        try {
            this.fix = new Metafix("nothing()");
            this.fix.setRepeatedFieldsToEntities(true);
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    @Override
    protected void onSetReceiver() {
        final TripleFilter tripleFilter = new TripleFilter();
        tripleFilter.setObjectPattern(objectPattern);
        fix
                .setReceiver(new StreamFlattener())
                .setReceiver(new StreamToTriples())
                .setReceiver(tripleFilter)
                .setReceiver(new ObjectTemplate<>("${p}\\t|\\t${o}"))
                .setReceiver(getReceiver());
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
}
