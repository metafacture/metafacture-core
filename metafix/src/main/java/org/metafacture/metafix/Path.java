/*
 * Copyright 2022 Fabian Steeg, hbz NRW
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

import org.metafacture.metafix.Value.Array;

import java.util.Arrays;
import java.util.function.UnaryOperator;

/**
 * Our goal here is something like https://metacpan.org/pod/Catmandu::Path::simple
 *
 * @author Fabian Steeg (fsteeg)
 *
 */
public class Path {
    private static final String ASTERISK = "*";
    private String[] path;

    public Path(final String[] path) {
        this.path = path;
    }

    public Value findInArray(final Value.Array array) {
        final Value result;
        if (path.length > 0) {
            final String currentSegment = path[0];
            if (currentSegment.equals(ASTERISK)) {
                result = Value.newArray(a -> array.forEach(v -> a.add(findInValue(v, tail(path)))));
            }
            else if (Value.isNumber(currentSegment)) {
                final int index = Integer.parseInt(currentSegment) - 1; // TODO: 0-based Catmandu vs. 1-based Metafacture
                if (index >= 0 && index < array.size()) {
                    result = findInValue(array.get(index), tail(path));
                }
                else {
                    result = null;
                }
            }
            // TODO: WDCD? copy_field('your.name','author[].name'), where name is an array
            else {
                result = Value.newArray(a -> array.forEach(v -> a.add(findInValue(v, path))));
            }
        }
        else {
            result = new Value(array);
        }
        return result;
    }

    private Value findInValue(final Value value, final String[] p) {
        // TODO: move impl into enum elements, here call only value.find
        return value == null ? null : value.extractType((m, c) -> m
                .ifArray(a -> c.accept(new Path(p).findInArray(a)))
                .ifHash(h -> c.accept(h.find(p)))
                .orElse(c)
        );
    }

    private String[] tail(final String[] fields) {
        return Arrays.copyOfRange(fields, 1, fields.length);
    }

    public void transformInArray(final Array array, final UnaryOperator<String> operator) {
        final String currentSegment = path[0];
        final int size = array.size();

        if (path.length == 0 || currentSegment.equals(ASTERISK)) {
            for (int i = 0; i < size; ++i) {
                transformValueAt(array, i, tail(path), operator);
            }
        }
        else if (Value.isNumber(currentSegment)) {
            final int index = Integer.parseInt(currentSegment) - 1; // TODO: 0-based Catmandu vs. 1-based Metafacture
            if (index >= 0 && index < size) {
                transformValueAt(array, index, tail(path), operator);
            }
        }
        // TODO: WDCD? copy_field('your.name','author[].name'), where name is an array
        else {
            for (int i = 0; i < size; ++i) {
                transformValueAt(array, i, tail(path), operator);
            }
        }

        array.getList().removeIf(v -> Value.isNull(v));
    }

    private void transformValueAt(final Array array, final int index, final String[] p, final UnaryOperator<String> operator) {
        final Value value = array.get(index);
        if (value != null) {
            value.matchType()
                .ifString(s -> array.set(index, operator != null ? new Value(operator.apply(s)) : null))
                .orElse(v -> new Value.TypeMatcher(v)
                        .ifArray(a -> new Path(p).transformInArray(a, operator))
                        .ifHash(h -> h.transformPath(p, operator))
                        .orElseThrow());
        }
    }
}
