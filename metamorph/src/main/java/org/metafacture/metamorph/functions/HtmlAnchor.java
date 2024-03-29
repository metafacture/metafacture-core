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

/**
 * Creates a html link based on the received value.
 *
 * @author Markus Michael Geipel
 *
 */
public final class HtmlAnchor extends AbstractCompose {

    private String title;

    /**
     * Creates an instance of {@link HtmlAnchor}.
     */
    public HtmlAnchor() {
    }

    /**
     * Sets the title.
     *
     * @param title the title.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public String process(final String value) {
        final String text = title == null ? value : title;
        return "<a href=\"" + getPrefix() + value + getPostfix() + "\">" + text + "</a>";
    }

}
