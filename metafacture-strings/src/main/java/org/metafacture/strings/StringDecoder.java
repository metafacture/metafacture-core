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

package org.metafacture.strings;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits a {@link String} into several {@link String}s, either by extracting
 * parts that match a regexp or by splitting by a regexp.
 *
 * @author Markus M Geipel
 */
@Description("Splits a String into several Strings, either by extracting parts that match a regexp or by splitting by a regexp.")
@In(String.class)
@Out(String.class)
@FluxCommand("decode-string")
public final class StringDecoder extends DefaultObjectPipe<String, ObjectReceiver<String>> {

    /**
     * Determines whether the string is split by the regexp or parts matching the
     * regexp are extracted.
     */
    public enum Mode {
        SPLIT, EXTRACT
    }

    private Mode mode = Mode.SPLIT;

    private final Pattern pattern;

    /**
     * Creates an instance of {@link StringDecoder} by a given pattern.
     *
     * @param pattern the pattern
     */
    public StringDecoder(final String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    /**
     * Sets the Mode.
     *
     * @param mode the {@link Mode}
     */
    public void setMode(final Mode mode) {
        this.mode = mode;
    }

    @Override
    public void process(final String obj) {
        assert !isClosed();
        assert null != obj;

        final ObjectReceiver<String> receiver = getReceiver();
        if (mode == Mode.SPLIT) {
            for (final String part : pattern.split(obj)) {
                receiver.process(part);
            }
        }
        else {
            final Matcher matcher = pattern.matcher(obj);
            while (matcher.find()) {
                receiver.process(matcher.group());
            }
        }
    }
}
