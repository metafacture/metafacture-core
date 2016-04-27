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
package org.culturegraph.mf.stream.reader;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.FluxCommand;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.stream.converter.xml.GenericXmlHandler;


/**
 * Generic reader for xml files.
 *
 * @author Markus Michael Geipel
 *
 */
@Description("Generic reader for xml files")
@In(java.io.Reader.class)
@Out(StreamReceiver.class)
@FluxCommand("generic-xml")
public class GenericXmlReader extends XmlReaderBase {
	public GenericXmlReader(final String recordTag) {
		super(new GenericXmlHandler(recordTag));
	}
}
