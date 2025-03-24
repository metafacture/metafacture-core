/* Copyright 2013 Pascal Christoph.
 * Licensed under the Eclipse Public License 1.0 */

package org.metafacture.biblio;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Opens an SRU (Search Retrieval by URL) stream and passes a reader to the receiver.
 * The input should be the base URL of the SRU service to be retrieved from.
 *
 * @author Pascal Christoph (dr0i)
 */
@Description("Opens a SRU stream and passes a reader to the receiver. The input should be the base URL of the SRU service to be retrieved from. Mandatory argument is: QUERY.")
@In(String.class)
@Out(Reader.class)
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
    private int totalRecords;

    private boolean stopRetrieving;


    /**
     * Creates an instance of {@link SruOpener}
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
            int retrievedRecords = 0;
            while (!stopRetrieving && (totalRecords==0 || retrievedRecords < totalRecords)) {
                if (totalRecords >0) {
                    int yetToRetrieveRecords = retrievedRecords - totalRecords;
                    if (yetToRetrieveRecords > maximumRecords) {
                        maximumRecords = yetToRetrieveRecords;
                    }
                }
                retrieve(srUrl, startRecord); //todo: bis max lookup zuviel (bis der nämlich sehr klein ist => keine Ergebnisse mehr)
                startRecord = startRecord + maximumRecords;
                retrievedRecords = retrievedRecords + maximumRecords;
            }
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    private void retrieve(StringBuilder srUrl, int startRecord) throws IOException {
        final URL urlToOpen = new URL(srUrl.toString() + "&maximumRecords=" + maximumRecords+"&startRecord=" + startRecord);
        final HttpURLConnection connection = (HttpURLConnection) urlToOpen.openConnection();

        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        if (!userAgent.isEmpty()) {
            connection.setRequestProperty("User-Agent", userAgent);
        }
        InputStream istream = getInputStream(connection);
        try (
            InputStreamReader inputStreamReader = new InputStreamReader(istream);
        ) {
            System.out.println("srUrl="+srUrl);
            System.out.println("startRecord="+startRecord);
            System.out.println("istream.length="+istream.available());
            if (istream.available() < 768){ // we take it that this is a result without a record
                stopRetrieving = true;
            }

            getReceiver().process(inputStreamReader);
        }
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
