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

package org.metafacture.metamorph.xml;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Filters out {@code comment} SAX events.
 *
 * @author Christoph Böhme
 *
 */
final class CommentsFilter extends LexicalHandlerXmlFilter {

    CommentsFilter(final XMLReader parent) {
        super(parent);
    }

    @Override
    public void comment(final char[] ch, final int start, final int length) throws SAXException {
        // Do not forward comment events
    }

}
