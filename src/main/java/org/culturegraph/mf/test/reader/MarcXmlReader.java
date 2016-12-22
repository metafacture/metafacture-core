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
package org.culturegraph.mf.test.reader;

import org.culturegraph.mf.biblio.marc21.MarcXmlHandler;

/**
 * Reads and decodes a <i>MARC-XML</i> document.
 *
 * @author Markus Michael Geipel
 *
 */
public class MarcXmlReader extends XmlReaderBase {

	public MarcXmlReader() {
		super(new MarcXmlHandler());
	}

}
