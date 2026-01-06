/* Copyright 2025 Pascal Christoph

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

package org.metafacture.io;

import org.metafacture.commons.ResourceUtil;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Opens an SRU (Search Retrieval by URL) stream and passes a reader to the receiver. Pages through the SRU.
 *
 * @author Pascal Christoph (dr0i)
 */
@Description(// checkstyle-disable-line ClassDataAbstractionCoupling|ClassFanOutComplexity
        "Opens a SRU stream and passes a reader to the receiver. The input is the base URL of the SRU service " +
                "to be retrieved from. Mandatory argument is: QUERY.\n" +
                "The output is an XML document holding the user defined \"maximumRecords\" as documents. If there are" +
                "more documents than defined by MAXIMUM_RECORDS and there are more documents wanted (defined by " +
                "\"totalRecords\") there will be consecutive XML documents output as it pages through the SRU.")
@In(String.class)
@Out(java.io.Reader.class)
@FluxCommand("open-sru")
public final class SruOpener extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final String OPERATION = "searchRetrieve";
    private static final String RECORD_SCHEMA = "MARC21-xml";
    private static final String USER_AGENT = "metafacture-core";
    private static final String VERSION = "2.0";
    private static final int MAXIMUM_RECORDS = 10;
    private static final int START_RECORD = 1;

    private final HttpOpener httpOpener = new HttpOpener();
    private final Map<String, String> queryParameters = new TreeMap<>();

    private DocumentBuilder docBuilder;

    private int startRecord = START_RECORD;
    private int totalRecords = Integer.MAX_VALUE;

    /**
     * Default constructor
     */
    public SruOpener() {
        setMaximumRecords(MAXIMUM_RECORDS);
        setOperation(OPERATION);
        setRecordSchema(RECORD_SCHEMA);
        setUserAgent(USER_AGENT);
        setVersion(VERSION);
    }

    /**
     * Sets the User Agent to use. <strong>Default value: {@value USER_AGENT}</strong>.
     *
     * @param userAgent a user agent to be used when opening a URL
     */
    public void setUserAgent(final String userAgent) {
        httpOpener.setHeader("User-Agent", userAgent);
    }

    /**
     * Sets the query of the search.
     * <strong>Setting a query is mandatory.</strong>
     *
     * @param query the query
     */

    public void setQuery(final String query) {
        queryParameters.put("query", query);
    }

    /**
     * Sets total number of records to be retrieved. <strong>Default value: indefinite (as in "all")
     * </strong>.
     *
     * @param totalRecords total number of records to be retrieved
     */
    public void setTotalRecords(final int totalRecords) {
        this.totalRecords = totalRecords;
    }

    /**
     * Sets the maximum of records returned in one lookup. <strong>Default value: {@value MAXIMUM_RECORDS}</strong>.
     * The lookup is repeated as long as {@link #maximumRecords} is less than {@link #totalRecords}.
     *
     * @param maximumRecords maximum of records returned in one lookup
     */
    public void setMaximumRecords(final int maximumRecords) {
        queryParameters.put("maximumRecords", String.valueOf(maximumRecords));
    }

    /**
     * Sets where to start when retrieving records. <strong>Default value: {@value START_RECORD}</strong>.
     *
     * @param startRecord where to start when retrieving records
     */
    public void setStartRecord(final int startRecord) {
        this.startRecord = startRecord;
    }

    /**
     * Sets the format of the retrieved record data. <strong>Default value: {@value RECORD_SCHEMA}</strong>.
     *
     * @param recordSchema the format of the data of the records
     */
    public void setRecordSchema(final String recordSchema) {
        queryParameters.put("recordSchema", recordSchema);
    }

    /**
     * Sets the kind of operation of the lookup. <strong>Default value: {@value OPERATION}</strong>.
     *
     * @param operation the kind of operation of the lookup
     */
    public void setOperation(final String operation) {
        queryParameters.put("operation", operation);
    }

    /**
     * Sets the version of the lookup. <strong>Default value: {@value VERSION}</strong>.
     *
     * @param version the version of the lookup
     */
    public void setVersion(final String version) {
        queryParameters.put("version", version);
    }

    @Override
    public void process(final String baseUrl) {
        if (queryParameters.get("query") == null) {
            throw new IllegalArgumentException("Missing mandatory parameter 'query'");
        }

        try {
            docBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        }
        catch (final ParserConfigurationException e) {
            throw new MetafactureException(e);
        }

        final StringBuilder urlBuilder = new StringBuilder(baseUrl).append("?");
        queryParameters.forEach((k, v) -> urlBuilder
                .append(URLEncoder.encode(k, StandardCharsets.UTF_8)).append("=")
                .append(URLEncoder.encode(v, StandardCharsets.UTF_8)).append("&"));
        urlBuilder.append("startRecord=");

        final String url = urlBuilder.toString();
        int recordsRetrieved = 0;
        int numberOfRecords = Integer.MAX_VALUE;

        while (recordsRetrieved < totalRecords && startRecord < numberOfRecords) {
            final AtomicReference<String> responseBody = new AtomicReference<>();

            final boolean successful = httpOpener.open(url + startRecord, r -> {
                try {
                    responseBody.set(ResourceUtil.readAll(r));
                }
                catch (final IOException e) {
                    throw new MetafactureException(e);
                }
            });

            try {
                try (Reader reader = new StringReader(responseBody.get())) {
                    final Document xmldoc = docBuilder.parse(new InputSource(reader));

                    numberOfRecords = getIntegerValueFromElement(xmldoc, "numberOfRecords", 0);
                    final int recordPosition = getIntegerValueFromElement(xmldoc, "recordPosition", 0);
                    final int nextRecordPosition = getIntegerValueFromElement(xmldoc, "nextRecordPosition", totalRecords);

                    recordsRetrieved = recordsRetrieved + nextRecordPosition - recordPosition;
                    startRecord = nextRecordPosition;
                }

                try (Reader reader = new StringReader(responseBody.get())) {
                    getReceiver().process(reader);
                }
            }
            catch (final IOException | SAXException e) {
                throw new MetafactureException(e);
            }

            if (!successful) {
                break;
            }
        }
    }

    private int getIntegerValueFromElement(final Document xmlDoc, final String tagName, final int fallback) {
        final Node node = xmlDoc.getElementsByTagName(tagName).item(0);
        return node != null ? Integer.parseInt(node.getTextContent()) : fallback;
    }

}
