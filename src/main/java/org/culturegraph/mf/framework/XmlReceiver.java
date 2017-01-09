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
package org.culturegraph.mf.framework;

import org.culturegraph.mf.framework.helpers.DefaultXmlReceiver;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * Interface for objects which receive XML events.
 * This is just a combination of the SAX handler interfaces
 * and the metastream {@link LifeCycle} interface.
 *
 * @see DefaultXmlReceiver
 * @see XmlPipe

 * @author Christoph Böhme
 *
 */
public interface XmlReceiver extends Receiver, ContentHandler, DTDHandler, EntityResolver, ErrorHandler, LexicalHandler {
	// Just a combination of LifeCycle and the SAX handler interfaces
}
