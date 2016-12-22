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
package org.culturegraph.mf.linkeddata;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;

/**
 * Expands some macros for RDF/XML
 *
 * @author Markus Michael Geipel
 *
 */
@Description("Expands some macros for RDF/XML")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("rdf-macros")
public final class RdfMacroPipe extends DefaultStreamPipe<StreamReceiver> {


	public static final char REFERENCE_MARKER = '*';
	public static final char LANGUAGE_MARKER = '$';
	public static final String RDF_REFERENCE = "~rdf:resource";
	public static final String RDF_ABOUT = "~rdf:about";
	public static final String XML_LANG = "~xml:lang";
	private String autoAddedSubject = "";

	public void setAutoAddedSubject(final String autoAddedSubject) {
		this.autoAddedSubject = autoAddedSubject;
	}


	@Override
	public void startRecord(final String identifier) {
		getReceiver().startRecord(identifier);
		if(!autoAddedSubject.isEmpty()){
			getReceiver().startEntity(autoAddedSubject);
			getReceiver().literal(RDF_ABOUT, identifier);
		}
	}

	@Override
	public void endRecord() {
		if(!autoAddedSubject.isEmpty()){
			getReceiver().endEntity();
		}
		getReceiver().endRecord();
	}

	@Override
	public void startEntity(final String name) {
		getReceiver().startEntity(name);
	}

	@Override
	public void endEntity() {
		getReceiver().endEntity();

	}

	@Override
	public void literal(final String name, final String value) {
		final int index = name.indexOf(LANGUAGE_MARKER);
		if (!name.isEmpty() && name.charAt(0) == REFERENCE_MARKER) {
			getReceiver().startEntity(name.substring(1));
			getReceiver().literal(RDF_REFERENCE, value);
			getReceiver().endEntity();
		} else if (index > 0) {
			getReceiver().startEntity(name.substring(0, index));
			getReceiver().literal(XML_LANG, name.substring(index + 1));
			getReceiver().literal("", value);
			getReceiver().endEntity();
		} else {
			getReceiver().literal(name, value);
		}
	}
}
