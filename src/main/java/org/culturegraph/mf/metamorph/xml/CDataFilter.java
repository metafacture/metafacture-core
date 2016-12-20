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
package org.culturegraph.mf.metamorph.xml;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Filters out CDATA events.
 *
 * Note that the actual contents of the CDATA section is
 * not filtered. This filter removes only the events
 * marking the start and end of CDATA sections. Their
 * contents are still passed as {@code characters} events.
 * This filter only hides the fact that the character data
 * comes from a CDATA section.
 *
 * @author Christoph Böhme
 *
 */
final class CDataFilter extends LexicalHandlerXmlFilter {

	CDataFilter(final XMLReader parent) {
		super(parent);
	}

	@Override
	public void startCDATA() throws SAXException {
		// Do not forward CDATA section events
	}

	@Override
	public void endCDATA() throws SAXException {
		// Do not forward CDATA section events
	}

}
