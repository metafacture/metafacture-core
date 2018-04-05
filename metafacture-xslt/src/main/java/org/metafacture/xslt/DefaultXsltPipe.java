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
package org.metafacture.xslt;

import net.sf.saxon.s9api.*;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.Receiver;
import org.metafacture.framework.helpers.DefaultXmlPipe;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.File;

public abstract class DefaultXsltPipe<R extends Receiver> extends DefaultXmlPipe<R>
{
    private Processor processor;
    private DocumentBuilder documentBuilder;
    private BuildingContentHandler buildingContentHandler;
    private XsltTransformer transformer;

    public DefaultXsltPipe(String stylesheetId)
    {
        this.processor = new Processor(false);
        this.documentBuilder = processor.newDocumentBuilder();
        try
        {
            this.transformer = processor
                    .newXsltCompiler()
                    .compile(new StreamSource(new File(stylesheetId)))
                    .load();
        }
        catch (SaxonApiException e)
        {
            throw  new MetafactureException(e);
        }
    }

    public Processor getProcessor()
    {
        return processor;
    }

    abstract Destination getDestination();

    @Override
    public void onSetReceiver()
    {
        transformer.setDestination(getDestination());
    }

    @Override
    public void startDocument()
    {
        try
        {
            buildingContentHandler = documentBuilder.newBuildingContentHandler();
            buildingContentHandler.startDocument();
        } catch (SaxonApiException|SAXException e)
        {
            throw new MetafactureException(e);
        }
    }

    @Override
    public void endDocument()
    {
        try
        {
            buildingContentHandler.endDocument();
            processor.writeXdmValue(buildingContentHandler.getDocumentNode(), transformer);
        }
        catch (SAXException|SaxonApiException e)
        {
            throw new MetafactureException(e);
        }
    }

    @Override
    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes attributes)
    {
        try
        {
            buildingContentHandler.startElement(uri, localName, qName, attributes);
        }
        catch (SAXException e)
        {
            throw new MetafactureException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
        try
        {
            buildingContentHandler.endElement(uri, localName, qName);
        }
        catch (SAXException e)
        {
            throw new MetafactureException(e);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
    {
        try
        {
            buildingContentHandler.characters(ch, start, length);
        }
        catch (SAXException e)
        {
            throw new MetafactureException(e);
        }
    }
}
