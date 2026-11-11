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

import org.metafacture.commons.types.ListMap;
import org.metafacture.metamorph.api.NamedValueSource;
import org.metafacture.metamorph.api.helpers.AbstractFlushingCollect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds the cross product of the data sources.
 *
 * @author Markus Michael Geipel
 *
 */
public final class Tuples extends AbstractFlushingCollect {

    public static final int MIN_N = 1;
    private final ListMap<String, String> listMap = new ListMap<>();

    private int minN = MIN_N;
    private String separator = "";

    /**
     * Creates an instance of {@link Tuples}.
     */
    public Tuples() {
    }

    /**
     * Sets min N. <strong>Default value: {@value #MIN_N}</strong>
     *
     * @param minN the min N
     */
    public void setMinN(final int minN) {
        this.minN = minN;
    }

    /**
     * Sets the separator.
     *
     * @param separator the separator
     */
    public void setSeparator(final String separator) {
        this.separator = separator;
    }

    @Override
    protected void receive(final String name, final String value,
            final NamedValueSource source) {
        listMap.add(name, value);
    }

    @Override
    protected boolean isComplete() {
        return false;
    }

    @Override
    protected void clear() {
        listMap.clear();

    }

    @Override
    protected void emit() {

        if (listMap.size() < minN) {
            return;
        }
        final List<String> keys = new ArrayList<String>();
        keys.addAll(listMap.keySet());
        Collections.sort(keys);

        List<String> temp = new ArrayList<String>();
        List<String> nextTemp = new ArrayList<String>();
        temp.add("");

        for (final String key : keys) {
            final List<String> values = listMap.get(key);
            nextTemp = new ArrayList<String>(temp.size() * values.size());
            for (final String value : values) {
                for (final String base : temp) {
                    nextTemp.add(base + separator + value);
                }
            }
            temp = nextTemp;
        }

        for (final String string : temp) {
            getNamedValueReceiver().receive(getName(),
                    string.substring(separator.length()), this,
                    getRecordCount(), getEntityCount());
        }
        clear();
    }

}
