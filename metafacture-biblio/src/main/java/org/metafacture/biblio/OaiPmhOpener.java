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

import org.oclc.oai.harvester2.app.RawWrite;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathException;

/**
 * Opens an OAI-PMH stream and passes a reader to the receiver.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
@Description("Opens an OAI-PMH stream and passes a reader to the receiver. Mandatory arguments are: BASE_URL, DATE_FROM, DATE_UNTIL, METADATA_PREFIX, SET_SPEC .")
@In(String.class)
@Out(java.io.Reader.class)
@FluxCommand("open-oaipmh")
public final class OaiPmhOpener extends DefaultObjectPipe<String, ObjectReceiver<Reader>> {

    private String encoding = "UTF-8";

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private String dateFrom;

    private String dateUntil;

    private String setSpec;

    private String metadataPrefix;

    /**
     * Default constructor
     */
    public OaiPmhOpener() {
    }

    /**
     * Sets the encoding to use. The default setting is UTF-8.
     *
     * @param encoding new default encoding
     */
    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the beginning of the retrieving of updated data. The form is
     * YYYY-MM-DD .
     *
     * @param dateFrom The form is YYYY-MM-DD .
     */
    public void setDateFrom(final String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * Sets the end of the retrieving of updated data. The form is YYYY-MM-DD .
     *
     * @param dateUntil The form is YYYY-MM-DD .
     */
    public void setDateUntil(final String dateUntil) {
        this.dateUntil = dateUntil;
    }

    /**
     * Sets the OAI-PM metadata prefix .
     *
     * @param metadataPrefix the OAI-PM metadata prefix
     */
    public void setMetadataPrefix(final String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    /**
     * Sets the OAI-PM set specification .
     *
     * @param setSpec th OAI-PM set specification
     */
    public void setSetSpec(final String setSpec) {
        this.setSpec = setSpec;
    }

    @Override
    public void process(final String baseUrl) {

        try {
            RawWrite.run(baseUrl, this.dateFrom, this.dateUntil, this.metadataPrefix, this.setSpec, outputStream);
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch (final SAXException e) {
            e.printStackTrace();
        }
        catch (final XPathException e) {
            e.printStackTrace();
        }
        catch (final NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            getReceiver().process(
                    new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray()), encoding));
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }
}
