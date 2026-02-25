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
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Description("Builds a Solr Document from metafacture stream events.")
@In(StreamReceiver.class)
@Out(SolrDocumentReceiver.class)
@FluxCommand("build-solr-doc")
public class SolrDocumentBuilder extends DefaultStreamPipe<ObjectReceiver<SolrInputDocument>> {

    private SolrInputDocument document;
    private String updateMethod;
    private String updateFieldName;
    private List<String> updateFieldValues;
    private Set<String> validUpdateMethods;

    /**
     * Creates an instance of {@link SolrDocumentBuilder}.
     */
    public SolrDocumentBuilder() {
        updateMethod = "";
        updateFieldValues = new ArrayList<>();

        // See also: https://lucene.apache.org/solr/guide/7_5/updating-parts-of-documents.html
        validUpdateMethods = new HashSet<>();
        validUpdateMethods.add("add");
        validUpdateMethods.add("add-distinct");
        validUpdateMethods.add("inc");
        validUpdateMethods.add("remove");
        validUpdateMethods.add("removeregexp");
        validUpdateMethods.add("set");
    }

    @Override
    public void startRecord(final String identifier) {
        document = new SolrInputDocument();
    }

    @Override
    public void endRecord() {
        getReceiver().process(document);
    }

    @Override
    public void startEntity(final String name) {
        if (!validUpdateMethods.contains(name)) {
            throw new MetafactureException("Invalid update method " + "'" + name  + "'" + "." +
                    "Use: add, add-distinct, inc, remove, removeregexp or set.");
        }
        updateMethod = name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void endEntity() {
        if (!updateMethod.isEmpty()) {

            final SolrInputField field = document.getField(updateFieldName);

            final boolean isSingleValue = updateFieldValues.size() == 1;
            final Object updateValue = isSingleValue ? updateFieldValues.get(0) : new ArrayList<>(updateFieldValues);

            // New field
            if (field == null) {
                final Map<String, Object> updates = new HashMap<>();
                updates.put(updateMethod, updateValue);
                document.addField(updateFieldName, updates);
            }
            // Modify field
            else {
                final Object obj = field.getValue();
                if (obj instanceof Map) {
                    final Map<String, Object> updates = (Map<String, Object>) obj;
                    updates.put(updateMethod, updateValue);
                }
            }
        }
        updateMethod = "";
        updateFieldValues = new ArrayList<>();
    }

    @Override
    public void literal(final String name, final String value) {
        if (updateMethod.isEmpty()) {
            document.addField(name, value);
        }
        else {
            updateFieldName = name;
            updateFieldValues.add(value);
        }
    }

    @Override
    public void onResetStream() {
        updateFieldValues.clear();
    }

    @Override
    public void onCloseStream() {
        updateFieldValues.clear();
    }
}
