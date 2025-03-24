/* Copyright 2013 Pascal Christoph.
 * Licensed under the Eclipse Public License 1.0 */

package org.metafacture.io;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Opens an SRU (Search Retrieval by URL) stream and passes a reader to the receiver. Pages through the SRU.
 *
 * @author Pascal Christoph (dr0i)
 */
@Description(
        "Opens a SRU stream and passes a reader to the receiver. The input is the base URL of the SRU service " +
                "to be retrieved from. Mandatory argument is: QUERY.\n" +
                "The output is an XML document holding the user defined \"maximumRecords\" as documents. If there are" +
                "more documents than defined by MAXIMUM_RECORDS and there are more documents wanted (defined by " +
                "\"totalRecords\") there will be consecutive XML documents output as it pages through the SRU.")
@In(String.class)
@Out(java.io.Reader.class)
@FluxCommand("open-sru")
public final class SruOpener extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

    private static final String OPERATION = "searchRetrieve";
    private static final String RECORD_SCHEMA = "MARC21-xml";
    private static final String USER_AGENT = "metafacture-core";
    private static final String VERSION = "2.0";

    private static final int CONNECTION_TIMEOUT = 11000;
    private static final int MAXIMUM_RECORDS = 10;
    private static final int START_RECORD = 1;
    private String operation = OPERATION;
    private String query;
    private String recordSchema = RECORD_SCHEMA;
    private String userAgent = USER_AGENT;
    private String version = VERSION;

    private int maximumRecords = MAXIMUM_RECORDS;
    private int startRecord = START_RECORD;
    private int totalRecords = Integer.MAX_VALUE;
    int numberOfRecords = Integer.MAX_VALUE;

    private boolean stopRetrieving;
    private int recordsRetrieved;

    private String xmlDeclarationTemplate = "<?xml version=\"%s\" encoding=\"%s\"?>";
    private String xmlDeclaration;

    /**
     * Default constructor
     */
    public SruOpener() {
    }

    /**
     * Sets the User Agent to use. <strong>Default value: {@value USER_AGENT}</strong>.
     *
     * @param userAgent a user agent to be used when opening a URL
     */
    public void setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Sets the query of the search.
     * <strong>Setting a query is mandatory.</strong>
     *
     * @param query the query
     */

    public void setQuery(final String query) {
        this.query = query;
    }

    /**
     * Sets total number of records to be retrieved. <strong>Default value: indefinite (as in "all")
     * </strong>.
     *
     * @param totalRecords total number of records to be retrieved
     */
    public void setTotal(final String totalRecords) {
        this.totalRecords = Integer.parseInt(totalRecords);
    }

    /**
     * Sets the maximum of records returned in one lookup. <strong>Default value: {@value MAXIMUM_RECORDS}</strong>.
     * The lookup is repeated as long as {@link #maximumRecords} is lesser than {@link #totalRecords}.
     *
     * @param maximumRecords maximum of records returned in one lookup
     */
    public void setMaximumRecords(final String maximumRecords) {
        this.maximumRecords = Integer.parseInt(maximumRecords);
    }

    /**
     * Sets where to start when retrieving records. <strong>Default value: {@value START_RECORD}</strong>.
     *
     * @param startRecord where to start when retrieving records
     */
    public void setStartRecord(final String startRecord) {
        this.startRecord = Integer.parseInt(startRecord);
    }

    /**
     * Sets the format of the retrieved record data. <strong>Default value: {@value RECORD_SCHEMA}</strong>.
     *
     * @param recordSchema the format of the data of the records
     */
    public void setRecordSchema(final String recordSchema) {
        this.recordSchema = recordSchema;
    }

    /**
     * Sets the kind of operation of the lookup. <strong>Default value: {@value OPERATION}</strong>.
     *
     * @param operation the kind of operation of the lookup
     */
    public void setOperation(final String operation) {
        this.operation = operation;
    }

    /**
     * Sets the version of the lookup. <strong>Default value: {@value VERSION}</strong>.
     *
     * @param version the version of the lookup
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public void process(final String baseUrl) {

        StringBuilder srUrl = new StringBuilder(baseUrl);
        if (query != null) {
            srUrl.append("?query=").append(query).append("&operation=").append(operation).append("&recordSchema=")
                .append(recordSchema).append("&version=").append(version);
        }
        else {
            throw new IllegalArgumentException("Missing mandatory parameter 'query'");
        }

        while (!stopRetrieving && recordsRetrieved < totalRecords && (startRecord < numberOfRecords)) {
            InputStream inputStream = getXmlDocsViaSru(srUrl);
            getReceiver().process(new InputStreamReader(inputStream));
        }

    }

    private InputStream getXmlDocsViaSru(final StringBuilder srUrl) {
        try {
            InputStream inputStreamOfURl = retrieveUrl(srUrl, startRecord, maximumRecords);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document xmldoc = docBuilder.parse(inputStreamOfURl);

            Transformer t = TransformerFactory.newInstance().newTransformer();
            StringWriter stringWriter = new StringWriter();
            t.transform(new DOMSource(xmldoc), new StreamResult(stringWriter));

            numberOfRecords = getIntegerValueFromElement(xmldoc,"numberOfRecords", 0);
            int recordPosition = getIntegerValueFromElement(xmldoc,"recordPosition", 0);
            int nextRecordPosition  = getIntegerValueFromElement(xmldoc,"nextRecordPosition", totalRecords);

            recordsRetrieved = recordsRetrieved + nextRecordPosition - recordPosition;
            startRecord = nextRecordPosition; // grenzwert : wenn maximumRcords > als in echt

            return new ByteArrayInputStream(stringWriter.toString().getBytes());

        }
        catch (final IOException | TransformerException | SAXException | ParserConfigurationException e) {
            throw new MetafactureException(e);
        }
    }

    private int getIntegerValueFromElement(final Document xmlDoc, final String tagName, final int fallback) {
        Node node = xmlDoc.getElementsByTagName(tagName).item(0);
        if (node != null) {
            return Integer.parseInt(node.getTextContent());
        }
        return fallback;
    }

    private InputStream retrieveUrl(StringBuilder srUrl, int startRecord, int maximumRecords) throws IOException {
        final URL urlToOpen =
                new URL(srUrl.toString() + "&maximumRecords=" + maximumRecords + "&startRecord=" + startRecord);
        final HttpURLConnection connection = (HttpURLConnection) urlToOpen.openConnection();

        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        if (!userAgent.isEmpty()) {
            connection.setRequestProperty("User-Agent", userAgent);
        }
        InputStream inputStream = getInputStream(connection);

        return inputStream;
    }

    private InputStream getInputStream(final HttpURLConnection connection) {
        try {
            return connection.getInputStream();
        }
        catch (final IOException e) {
            stopRetrieving = true;
            return connection.getErrorStream();
        }
    }

}
