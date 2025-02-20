/*
 * Copyright 2022 hbz
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

package org.metafacture.metafix.maps;

import org.metafacture.metafix.FixExecutionException;
import org.metafacture.metamorph.api.Maps;
import org.metafacture.metamorph.api.helpers.AbstractReadOnlyMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PropertyNotFoundException;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.UnaryOperator;

/**
 * Provides a dynamically built {@link Map} based on an RDF resource. Can be one file or a comma separated list of RDF
 * files or HTTP(S) URIs. Redirections of HTTP(S) URIs are followed.
 * The resources are supposed to be UTF-8 encoded.
 * <p>
 *
 * <strong>Important:</strong> When using a list of files make sure to set the proper separator. All lines that are not
 * split in two parts by the separator are ignored!
 *
 * @author Markus Michael Geipel
 * @author Pascal Christoph (dr0i)
 *
 * @see org.metafacture.metamorph.maps.FileMap
 */
public final class RdfMap extends AbstractReadOnlyMap<String, String> implements Closeable {

    public static final String SELECT = "select";
    public static final String TARGET = "target";
    public static final String TARGET_LANGUAGE = "select_language";

    private static final int MAX_REDIRECTIONS = 10;

    private static final int MIN_HTTP_STATUS_CODE = 299;
    private static final int MAX_HTTP_STATUS_CODE = 400;

    private final ArrayList<String> filenames = new ArrayList<>();
    private final Map<String, String> map = new HashMap<>();

    private Model model;
    private Select select = Select.DEFAULT;
    private String target;
    private String targetLanguage = "";
    private boolean isUninitialized = true;

    /**
     * Creates an instance of {@link RdfMap}.
     */
    public RdfMap() {
        setDefault(null);
    }

    private boolean isURI(final String name) {
        return name.toLowerCase().startsWith("http");
    }

    private void init() {
        filenames.forEach(this::loadFile);

        if (!isURI(target)) {
            final String[] nsPrefixAndProperty = target.split(":");
            target = nsPrefixAndProperty.length == 2 ? model.getNsPrefixURI(nsPrefixAndProperty[0]) + nsPrefixAndProperty[1] : nsPrefixAndProperty[0];
        }

        isUninitialized = false;
    }

    /**
     * Sets a comma separated list of files which provides the {@link Model}.
     *
     * @param files a comma separated list of files
     */
    public void setFiles(final String files) {
        Collections.addAll(filenames, files.split("\\s*,\\s*"));
    }

    /**
     * Sets a file which provides the {@link Model}.
     *
     * @param file the file
     */
    public void setResource(final String file) {
        filenames.add(file);
    }

    /**
     * Sets a file or URL which provides the {@link Model}.
     *
     * @param file the file or URL
     * @param operator an operator to apply to the file
     */
    public void setResource(final String file, final UnaryOperator<String> operator) {
        setResource(isURI(file) ? file : operator.apply(file));
    }

    private void loadFile(final String file) {
        try {
            final String uri = isURI(file) ? read(file) : file;

            if (model == null) {
                model = RDFDataMgr.loadModel(uri);
            }
            else {
                RDFDataMgr.read(model, uri);
            }
        }
        catch (final IOException e) {
            throw new FixExecutionException("Error while loading RDF file: " + file, e);
        }
    }

    /**
     * Builds a Map dynamically by querying an RDF model based on a key and a targeted Property
     * (to be set in {@link RdfMap#setTarget(String)}) and an optional language tag (to be set in
     * {@link RdfMap#setTargetLanguage}).
     * <br>
     * The Map acts as a cache.
     * <p>
     * To minimize the need of parameters three different querying modes are gone through. If one fails, the next one is
     * tried:
     * <p>
     * 1. Get the value of the targeted Property of a Subject
     * <br>
     * 2. Get the Subject matching a targeted Property value
     * <br>
     * 3. Get the value of a Property using the value of a targeted Property
     *
     * @param key the Property value, or a Subject, to be looked up
     */
    @Override
    public String get(final Object key) {
        final String resourceName = key.toString();
        String result = null;

        if (map.containsKey(resourceName)) {
            result = map.get(resourceName);
        }
        else {
            if (isUninitialized) {
                init();
            }

            final Resource resource = ResourceFactory.createResource(resourceName);
            final Property targetProperty = ResourceFactory.createProperty(target);

            try {
                if (select.equals(Select.SUBJECT)) {
                    result = getSubjectUsingPropertyAndLiteral(resourceName, targetProperty);
                }
                else {
                    // 1. try to get LITERAL using SUBJECT and PROPERTY
                    if (!targetLanguage.isEmpty()) {
                        result = model.getRequiredProperty(resource, targetProperty, targetLanguage).getString();
                    }
                    else {
                        result = model.getRequiredProperty(resource, targetProperty).getString();
                    }
                }
            }
            catch (final PropertyNotFoundException | NullPointerException | NoSuchElementException e) {
                // 2. try to get SUBJECT using PROPERTY and LITERAL
                if (select.equals(Select.DEFAULT)) {
                    result = getSubjectUsingPropertyAndLiteral(resourceName, targetProperty);
                }
                // 3. try to get LITERAL of PREDICATE A using PREDICATE B
                if (!select.equals(Select.SUBJECT)) {
                    if (result == null) {
                        result = getLiteralOfPredicateUsingOtherPredicate(resourceName, targetProperty);
                    }
                }
            }

            map.put(resourceName, result);
        }

        return result;
    }

    private String getLiteralOfPredicateUsingOtherPredicate(final String resourceName, final Property targetProperty) {
        final ResIterator iter = model.listSubjectsWithProperty(targetProperty);
        String result = map.get(Maps.DEFAULT_MAP_KEY);

        while (iter.hasNext()) {
            final Resource resource = iter.nextResource();
            final StmtIterator iterProp = resource.listProperties(targetProperty);

            while (iterProp.hasNext()) {
                final Statement stmt = iterProp.nextStatement();

                if (stmt.getObject().asLiteral().getString().equals(resourceName)) {
                    final StmtIterator subIterProp = resource.listProperties(targetProperty);

                    while (subIterProp.hasNext()) {
                        final Statement subStmt = subIterProp.nextStatement();

                        if (subStmt.getLanguage().equals(targetLanguage) && !subStmt.getString().equals(resourceName)) {
                            result = subStmt.getString();
                        }
                    }
                }
            }
        }

        return result;
    }

    private String getSubjectUsingPropertyAndLiteral(final String resourceName, final Property targetProperty) {
        final ResIterator iter = model.listSubjectsWithProperty(targetProperty);
        String result = map.get(Maps.DEFAULT_MAP_KEY);

        while (iter.hasNext()) {
            final Resource resource = iter.nextResource();
            final StmtIterator stmtIterator = resource.listProperties(targetProperty);

            while (stmtIterator.hasNext()) {
                final RDFNode node = stmtIterator.next().getObject();

                if (!targetLanguage.isEmpty()) {
                    if (node.asLiteral().toString().equals(resourceName + "@" + targetLanguage)) {
                        result = resource.getURI();
                        break;
                    }
                }
                else {
                    if (node.asLiteral().getString().equals(resourceName)) {
                        result = resource.getURI();
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the language of the target Property which is queried in the RDF. Valid values are defined by BCP47.
     *
     * @return the targeted language
     */
    public String getTargetLanguage() {
        return targetLanguage;
    }

    /**
     * Sets the language of the target Property which is queried in the RDF. Valid values are defined by BCP47.
     * <br>
     * Setting the language of the target Property is optional.
     *
     * @param targetLanguage the language of the target Property to be queried
     */
    public void setTargetLanguage(final String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    /**
     * Gets the target Property which is queried in the RDF. Namespaces are allowed.
     *
     * @return the target Property to be queried
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the target Property which is queried in the RDF. Namespaces are allowed.
     * <br>
     * <strong>Setting a target Property is mandatory.</strong>
     *
     * @param target the Property to be queried
     */
    public void setTarget(final String target) {
        this.target = target;
    }

    /**
     * Gets whether the Subject or the Object or a mixture of both should be retrieved in the RDF.
     * <br>
     * Setting "select" is optional.
     *
     * @return the selected position to be retrieved
     **/
    public String getSelect() {
        return select.toString();
    }

    /**
     * Sets whether the Subject or the Object or a mixture of both should be retrieved in the RDF.
     * <br>
     * Setting "select" is optional.
     * <strong>Defaults to retrieve both: tries to get "objects" and as a fallback "subjects".</strong>
     *
     * @param position the position to be retrieved. Can be "subject" or "object".
     */
    public void setSelect(final String position) {
        if (Select.SUBJECT.name().equalsIgnoreCase(position)) {
            select = Select.SUBJECT;
        }
        else if (Select.OBJECT.name().equalsIgnoreCase(position)) {
            select = Select.OBJECT;
        }
        else {
            throw new FixExecutionException("Couldn't set parameter - use 'subject' or 'object' as value");
        }
    }

    /**
     * Sets the default value returned if the key couldn't be found.
     * <br>
     * <strong>Default value: {@link Maps#DEFAULT_MAP_KEY} </strong>
     *
     * @param defaultValue the default value returned
     */
    public void setDefault(final String defaultValue) {
        map.put(Maps.DEFAULT_MAP_KEY, defaultValue);
    }

    /**
     * Gets a redirected URL, if any redirection takes place. Adapted predated code from org.apache.jena.rdfxml.xmlinput.JenaReader.
     * <p>
     * Note: Using newer Jena version (needs java 11) this method would be obsolete.
     *
     * @param url the URL to resolve
     * @return the (redirected) URL
     * @throws IOException if any IO error occurs
     */
    private String read(final String url) throws IOException {
        String connectionURL = url;

        int count = 0;
        URLConnection conn;

        while (true) {
            final URLConnection conn2 = new URL(connectionURL).openConnection();
            if (!(conn2 instanceof HttpURLConnection)) {
                conn = conn2;
                break;
            }

            count += 1;
            if (count > MAX_REDIRECTIONS) {
                throw new IOException("Too many redirects followed for " + url);
            }

            final HttpURLConnection httpURLConnection = (HttpURLConnection) conn2;
            conn2.setRequestProperty("accept", "*/*");

            final int statusCode = httpURLConnection.getResponseCode();
            if (statusCode <= MIN_HTTP_STATUS_CODE || statusCode >= MAX_HTTP_STATUS_CODE) {
                conn = conn2;
                break;
            }

            // Redirect
            connectionURL = conn2.getHeaderField("Location");
            if (connectionURL == null || url.equals(connectionURL)) {
                throw new IOException("Failed to follow redirects for " + url);
            }
        }

        return conn.getURL().toString();
    }

    @Override
    public void close() {
        map.clear();
        model.close();
    }

    private enum Select {
        SUBJECT, OBJECT, DEFAULT
    }

}
