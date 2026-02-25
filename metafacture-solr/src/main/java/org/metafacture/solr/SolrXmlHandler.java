/*
 * Copyright 2018 Deutsche Nationalbibliothek
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

package org.metafacture.solr;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultXmlPipe;

import org.apache.solr.common.SolrInputDocument;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts XML Solr Documents for Index Updates to a Metafacture Stream.
 *
 * Encodes atomic index updates. See also: https://wiki.apache.org/solr/UpdateXmlMessages#Optional_attributes_for_.22add.22
 */
@Description("A handler for XML formatted index updates for Apache Solr.")
@In(XmlReceiver.class)
@Out(SolrDocumentReceiver.class)
@FluxCommand("handle-solr-xml")
public class SolrXmlHandler extends DefaultXmlPipe<ObjectReceiver<SolrInputDocument>> {
    private static final String DOC = "doc";
    private static final String FIELD = "field";
    private static final String NO_MODIFICATION = "";

    private SolrInputDocument solrDocument;

    /** Document depth (documents in a document). */
    private int documentDepth = 0;

    /** Stores the characters of the current XML element. */
    private StringBuilder characters = new StringBuilder();

    /** Local name of the current XML element */
    private String currentElement = "";

    /** Value of the name attribute for a field element */
    private String fieldName;
    /** A flag that indicates a atomic update */

    private boolean isModified;
    /**
     * Field modifier. May be 'inc', 'add', 'set', 'remove' or 'removeregexp'.
     * @see <a href="https://lucene.apache.org/solr/guide/7_0/updating-parts-of-documents.html#atomic-updates">Atomic Updates</a>
     */
    private String fieldModifier;

    private Map<String, Map<String, Object>> fieldUpdatesMap;

    /**
     * Creates an instance of {@link SolrXmlHandler}.
     */
    public SolrXmlHandler() {
        this.isModified = false;
    }

    /**
     * Creates an instance of {@link startElement}.
     * @param uri ???
     * @param localName ???
     * @param qName ???
     * @param attributes ???
     */
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
        currentElement = localName;
        switch (localName) {
            case DOC:
                solrDocument = new SolrInputDocument();
                fieldUpdatesMap = new HashMap<>();
                documentDepth++;
                if (documentDepth == 2) {
                    throw new MetafactureException("Nested documents are not supported!");
                }
                break;
            case FIELD:
                fieldName = attributes.getValue("name");
                isModified = attributes.getValue("update") != null;
                if (isModified) {
                    fieldModifier = attributes.getValue("update");
                }
                break;
            default:
                break;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void endElement(final String uri, final String localName, final String qName) {
        currentElement = localName;

        if (currentElement.equals(DOC)) {
            if (documentDepth == 1) {
                if (!fieldUpdatesMap.isEmpty()) {
                    for (final Map.Entry<String, Map<String, Object>> entry : fieldUpdatesMap.entrySet()) {
                        final String fieldName = entry.getKey();
                        final Map<String, Object> fieldUpdates = entry.getValue();
                        solrDocument.addField(fieldName, fieldUpdates);
                    }
                }
                getReceiver().process(solrDocument);
                reset();
            }
            else {
                throw new MetafactureException("Nested documents are not supported!");
            }
            documentDepth--;
        }
        else if (currentElement.equals(FIELD)) {
            final String fieldValue = characters.toString().trim();
            if (!isModified) {
                solrDocument.addField(fieldName, fieldValue);
            }
            else {
                final Map<String, Object> fieldUpdates = fieldUpdatesMap.getOrDefault(fieldName, new HashMap<>());
                if (fieldUpdates.isEmpty() || !fieldUpdates.containsKey(fieldModifier)) {
                    fieldUpdates.put(fieldModifier, fieldValue);
                    fieldUpdatesMap.put(fieldName, fieldUpdates);
                }
                else {
                    final Object existingValue = fieldUpdates.get(fieldModifier);
                    if (existingValue instanceof List) {
                        final List<String> existingValues = (List<String>) existingValue;
                        existingValues.add(fieldValue);
                    }
                    else {
                        final List list = new ArrayList();
                        list.add(existingValue);
                        list.add(fieldValue);
                        fieldUpdates.put(fieldModifier, list);
                    }
                }
            }
        }
        characters.setLength(0);
    }

    @Override
    public void characters(final char[] chars, final int start, final int length) {
        characters.append(chars, start, length);
    }

    private void reset() {
        solrDocument = new SolrInputDocument();
        fieldUpdatesMap.clear();
        fieldModifier = NO_MODIFICATION;
    }
}
