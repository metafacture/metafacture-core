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

package org.metafacture.statistics;

import org.metafacture.framework.helpers.DefaultStreamReceiver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Counts entity names, literal names, or literal values.
 *
 * @author Christoph Böhme
 */
public final class Histogram extends DefaultStreamReceiver {

    private final Map<String, Integer> histogram = new HashMap<>();

    private boolean countEntities;
    private boolean countLiterals;
    private String countField;

    /**
     * Creates an instance of {@link Histogram}.
     */
    public Histogram() {
    }

    /**
     * Creates an instance of {@link Histogram} with a field whose content is
     * counted.
     *
     * @param countField name of the field whose content is counted
     */
    public Histogram(final String countField) {
        setCountField(countField);
    }

    /**
     * Gets the histogram.
     *
     * @return the histogram
     */
    public Map<String, Integer> getHistogram() {
        return Collections.unmodifiableMap(histogram);
    }

    /**
     * Checks wether entities are counted.
     *
     * @return true if entities are counted.
     */
    public boolean isCountEntities() {
        return countEntities;
    }

    /**
     * Flags wether entities should be counted.
     *
     * @param countEntities true if entities should be counted.
     */
    public void setCountEntities(final boolean countEntities) {
        this.countEntities = countEntities;
    }

    /**
     * Checks wether literals are counted.
     *
     * @return true if literals are counted
     */
    public boolean isCountLiterals() {
        return countLiterals;
    }

    /**
     * Flags wether to count literals.
     *
     * @param countLiterals true if literals should be counted
     */
    public void setCountLiterals(final boolean countLiterals) {
        this.countLiterals = countLiterals;
    }

    /**
     * Gets the name of the field whose content is counted.
     *
     * @return the name of the field whose content is counted
     */
    public String getCountField() {
        return countField;
    }

    /**
     * Set the name of the field whose content is counted.
     *
     * @param countField the name of the field whose content is counted
     */
    public void setCountField(final String countField) {
        this.countField = countField;
    }

    @Override
    public void startEntity(final String name) {
        if (countEntities) {
            count(name);
        }
    }

    @Override
    public void literal(final String name, final String value) {
        if (countLiterals) {
            count(name);
        }
        if (name.equals(countField)) {
            count(value);
        }
    }

    @Override
    public void resetStream() {
        histogram.clear();
    }

    private void count(final String value) {
        Integer c = histogram.get(value);
        if (c == null) {
            c = Integer.valueOf(0);
        }
        histogram.put(value, Integer.valueOf(c.intValue() + 1));
    }

}
