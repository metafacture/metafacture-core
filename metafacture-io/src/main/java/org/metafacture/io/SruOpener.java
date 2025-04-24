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
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Opens an SRU (Search Retrieval by URL) stream and passes a reader to the receiver.
 * The input should be the base URL of the SRU service to be retrieved from.
 *
 * @author Pascal Christoph (dr0i)
 */
@Description("Opens a SRU stream and passes a reader to the receiver. The input should be the base URL of the SRU service to be retrieved from. Mandatory argument is: QUERY.")
@In(String.class)
@Out(java.io.Reader.class)
@FluxCommand("open-sru")
public final class SruOpener extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

    private static final String OPERATION = "searchRetrieve";
    private static final String RECORD_SCHEMA = "MARC21-xml";
    private static final String USER_AGENT = "";
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

    private boolean stopRetrieving;


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
     * Sets total number of records to be retrieved. <strong>Default value: indefinite (as in "all")</strong>.
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

        try {

            StringBuilder srUrl = new StringBuilder(baseUrl);
            if (query != null) {
                srUrl.append("?query=").append(query).append("&operation=").append(operation).append("&recordSchema=").append(recordSchema).append("&version=").append(version);
            }
            else {
                throw new IllegalArgumentException("Missing mandatory parameter 'query'");
            }
            int numberOfRecords = Integer.MAX_VALUE;
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            while (!stopRetrieving && (startRecord <  numberOfRecords)) {
        /*        if (totalRecords >0)  {
                    yetToRetrieveRecords = totalRecords - retrievedRecords;
                    if (yetToRetrieveRecords < maximumRecords) {
                        maximumRecords = yetToRetrieveRecords;
                    }
                }*/
                ByteArrayInputStream byteArrayInputStream = retrieve(srUrl, startRecord, maximumRecords);


                DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = factory.newDocumentBuilder();
                Document xmldoc = docBuilder.parse(byteArrayInputStream);

                Element element = (Element)xmldoc.getElementsByTagName("numberOfRecords").item(0);
                numberOfRecords=Integer.parseInt(element.getTextContent());

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                Result result = new StreamResult(os);
                t.transform(new DOMSource(xmldoc), result);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(os.toByteArray());

                getReceiver().process(
                    new InputStreamReader(inputStream));
                tf = TransformerFactory.newInstance();
                t = tf.newTransformer();
                t.setOutputProperty("omit-xml-declaration", "yes");
                startRecord = startRecord + maximumRecords;
            }
        }
        catch (final IOException | TransformerException | SAXException | ParserConfigurationException e) {
            throw new MetafactureException(e);
        }

    }

    private ByteArrayInputStream retrieve(StringBuilder srUrl, int startRecord, int maximumRecords) throws IOException {
        final URL urlToOpen = new URL(srUrl.toString() + "&maximumRecords=" + maximumRecords+"&startRecord=" + startRecord);
        final HttpURLConnection connection = (HttpURLConnection) urlToOpen.openConnection();

        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        if (!userAgent.isEmpty()) {
            connection.setRequestProperty("User-Agent", userAgent);
        }
        InputStream inputStream = getInputStream(connection);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            System.out.println("srUrl="+srUrl);
            System.out.println("startRecord="+startRecord);
            System.out.println("istream.length="+inputStream.available());
            if (inputStream.available() < 768){ // we take it that this is a result without a record
                stopRetrieving = true;
            }
        inputStream.transferTo(outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
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
