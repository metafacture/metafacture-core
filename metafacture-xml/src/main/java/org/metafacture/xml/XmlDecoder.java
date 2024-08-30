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

package org.metafacture.xml;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.Reader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Reads an XML file and passes the XML events to a receiver.
 *
 * @author Christoph Böhme
 *
 */
@Description("Reads an XML file and passes the XML events to a receiver. Set 'totalEntitySizeLimit=\"0\"' to allow unlimited XML entities.")
@In(Reader.class)
@Out(XmlReceiver.class)
@FluxCommand("decode-xml")
public final class XmlDecoder extends DefaultObjectPipe<Reader, XmlReceiver> {

    private static final String SAX_PROPERTY_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
    private static final String TOTAL_ENTITY_SIZE_LIMIT = "http://www.oracle.com/xml/jaxp/properties/totalEntitySizeLimit";
    private final XMLReader saxReader;

    /**
     * Creates an instance of {@link XmlDecoder} by obtaining a new instance of an
     * {@link org.xml.sax.XMLReader}.
     */
    public XmlDecoder() {
        try {
            final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            saxReader = parserFactory.newSAXParser().getXMLReader();
        }
        catch (final ParserConfigurationException | SAXException e) {
            throw new MetafactureException(e);
        }
    }

    /**
     * Sets the total entity size limit for the XML parser.
     * See <a href="https://docs.oracle.com/en/java/javase/13/security/java-api-xml-processing-jaxp-security-guide.html#GUID-82F8C206-F2DF-4204-9544-F96155B1D258__TABLE_RQ1_3PY_HHB">java-api-xml-processing-jaxp-security-guide.html</a>
     *
     * Defaults to "50,000,000". Set to "0" to allow unlimited entities.
     *
     * @param totalEntitySizeLimit the size of the allowed entities. Set to "0" if entities should be unlimited.
     */
    public void setTotalEntitySizeLimit(final String totalEntitySizeLimit) {
        try {
            saxReader.setProperty(TOTAL_ENTITY_SIZE_LIMIT, totalEntitySizeLimit);
        }
        catch (final SAXException e) {
            throw new MetafactureException(e);
        }
    }

    @Override
    public void process(final Reader reader) {
        try {
            saxReader.parse(new InputSource(reader));
        }
        catch (final IOException | SAXException e) {
            throw new MetafactureException(e);
        }
    }

    @Override
    protected void onSetReceiver() {
        saxReader.setContentHandler(getReceiver());
        saxReader.setDTDHandler(getReceiver());
        saxReader.setEntityResolver(getReceiver());
        saxReader.setErrorHandler(getReceiver());
        try {
            saxReader.setProperty(SAX_PROPERTY_LEXICAL_HANDLER, getReceiver());
        }
        catch (final SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new MetafactureException(e);
        }
    }

}
