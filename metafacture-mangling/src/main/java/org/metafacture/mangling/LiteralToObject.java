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

package org.metafacture.mangling;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Emits the values of literals matching {@literal #setPattern(String)}
 * as objects.
 * <p>
 * The matcher does only match the literal name and does not
 * take the enclosing entities into account.
 *
 * @author Christoph Böhme
 *
 */
@Description("Emits literal values as objects.")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("literal-to-object")
public final class LiteralToObject extends DefaultStreamPipe<ObjectReceiver<String>> {

    /**
     * Default value for {@link #setPattern(String)}.
     */
    public static final java.lang.String DEFAULT_PATTERN = ".*";

    private Matcher matcher = Pattern.compile(DEFAULT_PATTERN).matcher("");

    /**
     * Creates an instance of {@link LiteralToObject}.
     */
    public LiteralToObject() {
    }

    /**
     * Sets the pattern against which literal names are matched. Only the
     * values of matching literals are converted into objects.
     * <p>
     * The parameter can be changed at any time during processing. It becomes
     * effective with the next literal being processed.
     * <p>
     * The default pattern matches all literal names including empty ones.
     *
     * @param pattern a Java regular expression
     */
    public void setPattern(final String pattern) {
        this.matcher = Pattern.compile(pattern).matcher("");
    }

    /**
     * Gets the pattern against which literal names are matched.
     *
     * @return the pattern
     */
    public String getPattern() {
        return matcher.pattern().pattern();
    }

    @Override
    public void literal(final String name, final String value) {
        assert !isClosed();
        matcher.reset(name);
        if (matcher.matches()) {
            getReceiver().process(value);
        }
    }

}
