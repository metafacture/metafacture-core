/*
 * Copyright 2013, 2023 Deutsche Nationalbibliothek et al
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

import org.metafacture.metamorph.api.helpers.AbstractSimpleStatelessFunction;

import com.google.common.net.PercentEscaper;

/**
 * URL encodes the received value.
 * Default is to convert a whitespace " "to a plus sign "+". This can be set so that a whitespace " " is escaped to
 * "%20".
 * Safe characters for this escaper are the ranges 0..9, a..z and A..Z. These are always safe and should not be
 * specified. Default safe characters are also ".", "-", "*", and "_", following URLEncoder.
 *
 * @see java.net.URLEncoder
 *
 * @author Markus Michael Geipel
 * @author Pascal Christoph (dr0i)
 */
public final class URLEncode extends AbstractSimpleStatelessFunction {
    private String safeChars = ".-*_";
    private Boolean plusForSpace = true;
    private PercentEscaper percentEscaper = new PercentEscaper(safeChars, plusForSpace);

    /**
     * Creates an instance of {@link URLEncode}.
     */
    public URLEncode() {
    }

    @Override
    public String process(final String value) {
        return percentEscaper.escape(value);
    }

    /**
     * Sets a URI escaper with the specified safe characters. The ranges 0..9, a..z and A..Z are always safe
     * and should not be specified.
     *
     * Default is also ".", "-", "*", and "_" , mimicking {@link java.net.URLEncoder}.
     *
     * @param safeChars the chars which will not be escaped
     */
    public void setSafeChars(final String safeChars) {
        this.safeChars = safeChars;
        percentEscaper = new PercentEscaper(safeChars, plusForSpace);
    }

    /**
     * Sets if a space should be converted into a plus sign "+" or percent escaped as "%20".
     * <p>
     * Default is "true", i.e. to escape the space character as "+".
     *
     * @param plusForSpace true if space character " " should be converted into a plus sign "+"
     */
    public void setPlusForSpace(final Boolean plusForSpace) {
        this.plusForSpace = plusForSpace;
        percentEscaper = new PercentEscaper(safeChars, plusForSpace);
    }
}
