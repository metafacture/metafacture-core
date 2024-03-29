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

package org.metafacture.metamorph.functions;

import org.metafacture.commons.StringUtil;
import org.metafacture.metamorph.api.MorphBuildException;
import org.metafacture.metamorph.api.helpers.AbstractStatefulFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Only outputs the received values in a certain range.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
public final class Occurrence extends AbstractStatefulFunction {

    public static final String LESS_THAN = "lessThan ";
    public static final String MORE_THAN = "moreThan ";
    public static final boolean SAME_ENTITY = false;
    private int count;
    private String format;

    private IntFilter filter = new IntFilter() {
        @Override
        public boolean accept(final int value) {
            return true;
        }
    };

    private final Map<String, String> variables = new HashMap<>();
    private boolean sameEntity = SAME_ENTITY;

    /**
     * Creates an instance of {@link Occurrence}.
     */
    public Occurrence() {
    }

    @Override
    public String process(final String value) {
        ++count;
        if (filter.accept(count)) {
            return processMatch(value);
        }
        return null;
    }

    private String processMatch(final String value) {
        if (format == null) {
            return value;
        }
        variables.put("value", value);
        variables.put("count", String.valueOf(count));
        return StringUtil.format(format, variables);
    }

    /**
     * Sets the format.
     *
     * @param format the format
     */
    public void setFormat(final String format) {
        this.format = format;
    }

    @Override
    protected void reset() {
        count = 0;
    }

    @Override
    protected boolean doResetOnEntityChange() {
        return sameEntity;
    }

    /**
     * Sets the inequality. Use values starting with {@value #LESS_THAN} or
     * {@value #MORE_THAN}. If the value is an integer it an equal filter will be
     * set.
     *
     * @param only set the inequality, otherwise an equal filter will be used
     */
    public void setOnly(final String only) {
        filter = parse(only);
    }

    /**
     * Sets if the occurrence must be in the same entity. <strong>Default value:
     * {@value #SAME_ENTITY}</strong>
     *
     * @param sameEntity true if the occurrence must be in the same entity
     */
    public void setSameEntity(final boolean sameEntity) {
        this.sameEntity = sameEntity;
    }

    private static IntFilter parse(final String only) {
        final IntFilter filter;

        if (only.startsWith(LESS_THAN)) {
            filter = createLessThanFilter(extractNumberFrom(only));
        }
        else if (only.startsWith(MORE_THAN)) {
            filter = createGreaterThanFilter(extractNumberFrom(only));
        }
        else {
            final int number = Integer.parseInt(only);
            filter = createEqualsFilter(number);
        }
        return filter;
    }

    private static int extractNumberFrom(final String string) {
        final String[] tokens = string.split(" ", 2);
        if (tokens.length < 2) {
            throw new MorphBuildException("Invalid only string: " + string);
        }
        return Integer.parseInt(tokens[1]);
    }

    private static IntFilter createEqualsFilter(final int number) {
        return new IntFilter() {
            @Override
            public boolean accept(final int value) {
                return value == number;
            }
        };
    }

    private static IntFilter createLessThanFilter(final int number) {
        return new IntFilter() {
            @Override
            public boolean accept(final int value) {
                return value < number;
            }
        };
    }

    private static IntFilter createGreaterThanFilter(final int number) {
        return new IntFilter() {
            @Override
            public boolean accept(final int value) {
                return value > number;
            }
        };
    }

    /**
     * Filter for integer values
     */
    private interface IntFilter {
        boolean accept(int value);
    }

}
