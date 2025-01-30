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

package org.metafacture.linkeddata;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

/**
 * Expands some macros for RDF/XML
 *
 * @author Markus Michael Geipel
 *
 */
@Description("Expands some macros for RDF/XML. When using fix configure `referenceMarker` to any char but the default `*`")
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
    private char referenceMarker = REFERENCE_MARKER;
    private char languageMarker = LANGUAGE_MARKER;

    /**
     * Creates an instance of {@link RdfMacroPipe}.
     */
    public RdfMacroPipe() {
    }

    /**
     * Flas whether to auto add the subject.
     *
     * @param autoAddedSubject true if subject shoudl be auto added
     */
    public void setAutoAddedSubject(final String autoAddedSubject) {
        this.autoAddedSubject = autoAddedSubject;
    }

    /**
     * Sets the single char reference marker.
     *
     * @param referenceMarker the reference marker
     */
    public void setReferenceMarker(final String referenceMarker) {
        this.referenceMarker = referenceMarker.charAt(0);
    }

    /**
     * Gets the reference marker.
     *
     * @return the reference marker
     */
    public char getReferenceMarker() {
        return referenceMarker;
    }

    /**
     * Sets the single char language marker.
     *
     * @param languageMarker the language marker
     */
    public void setLanguageMarker(final String languageMarker) {
        this.languageMarker = languageMarker.charAt(0);
    }

    /**
     * Gets the language marker.
     *
     * @return the language marker
     */
    public char getLanguageMarker() {
        return languageMarker;
    }

    @Override
    public void startRecord(final String identifier) {
        getReceiver().startRecord(identifier);
        if (!autoAddedSubject.isEmpty()) {
            getReceiver().startEntity(autoAddedSubject);
            getReceiver().literal(RDF_ABOUT, identifier);
        }
    }

    @Override
    public void endRecord() {
        if (!autoAddedSubject.isEmpty()) {
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
        final int index = name.indexOf(languageMarker);
        if (!name.isEmpty() && name.charAt(0) == referenceMarker) {
            getReceiver().startEntity(name.substring(1));
            getReceiver().literal(RDF_REFERENCE, value);
            getReceiver().endEntity();
        }
        else if (index > 0) {
            getReceiver().startEntity(name.substring(0, index));
            getReceiver().literal(XML_LANG, name.substring(index + 1));
            getReceiver().literal("", value);
            getReceiver().endEntity();
        }
        else {
            getReceiver().literal(name, value);
        }
    }
}
