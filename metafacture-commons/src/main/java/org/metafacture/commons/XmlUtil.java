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

package org.metafacture.commons;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.util.stream.Collectors;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Utility functions for working with XML data as strings.
 *
 * @author Christoph Böhme
 */
public final class XmlUtil {

    private static final String APPLICATION_XML_MIME_TYPE = "application/xml";
    private static final String TEXT_XML_MIME_TYPE = "text/xml";
    private static final String XML_BASE_MIME_TYPE = "+xml";

    private static final int ESCAPE_CODE_POINT_THRESHOLD = 0x7f;

    private XmlUtil() {
        // No instances allowed
    }

    public static String nodeToString(final Node node) {
        return nodeToString(node, false);
    }

    public static String nodeToString(final Node node,
            final boolean omitXMLDecl) {
        final StringWriter writer = new StringWriter();
        final Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        }
        catch (final TransformerException e) {
            throw new AssertionError(
                    "No errors expected when creating an identity transformer", e);
        }

        if (omitXMLDecl) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        else {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        }

        try {
            transformer.transform(new DOMSource(node), new StreamResult(writer));
        }
        catch (final TransformerException e) {
            throw new AssertionError(
                    "No errors expected during identity transformation", e);
        }

        return writer.toString();
    }

    public static String nodeListToString(final NodeList nodes) {
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < nodes.getLength(); ++i) {
            builder.append(nodeToString(nodes.item(i), i != 0));
        }

        return builder.toString();
    }

    public static boolean isXmlMimeType(final String mimeType) {
        if (mimeType == null) {
            return false;
        }
        return APPLICATION_XML_MIME_TYPE.equals(mimeType) ||
                TEXT_XML_MIME_TYPE.equals(mimeType) ||
                mimeType.endsWith(XML_BASE_MIME_TYPE);
    }

    public static String escape(final String unescaped) {
        return escape(unescaped, true);
    }

    public static String escape(final String unescaped, final boolean escapeUnicode) {
        return unescaped.codePoints()
                .mapToObj(value -> escapeCodePoint(value, escapeUnicode))
                .collect(Collectors.joining());
    }

    private static String escapeCodePoint(final int codePoint, final boolean escapeUnicode) {
        final String entity = entityFor(codePoint);
        if (entity != null) {
            return entity;
        }
        return escapeUnicode && codePoint > ESCAPE_CODE_POINT_THRESHOLD ?
            "&#" + Integer.toString(codePoint) + ";" : Character.toString((char) codePoint);
    }

    private static String entityFor(final int ch) {
        final String entity;

        switch (ch) {
            case '<':
                entity = "&lt;";
                break;
            case '>':
                entity = "&gt;";
                break;
            case '&':
                entity = "&amp;";
                break;
            case '"':
                entity = "&quot;";
                break;
            case '\'':
                entity = "&apos;";
                break;
            default:
                entity = null;
        }

        return entity;
    }

}
