package org.metafacture.xml;

import java.util.Collections;

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.helpers.DefaultStreamPipe;

/**
 * Encode a stream to XML.
 */

@Description("Encode a stream to XML.")
@In(StreamReceiver.class)
public final class XmlEncoder extends DefaultStreamPipe<ObjectReceiver<String>>
{
    private static final String ROOT_OPEN = "<records>";
    private static final String ROOT_CLOSE = "</records>";

    private static final String RECORD_OPEN_TEMPLATE= "<record id=\"%s\">";
    private static final String RECORD_CLOSE= "</record>";

    private static final String ENTITIY_OPEN_TEMPLATE = "<entity name=\"%s\">";
    private static final String ENTITY_CLOSE = "</entity>";

    private static final String LITERAL_OPEN_TEMPLATE = "<literal name=\"%s\">";
    private static final String LITERAL_CLOSE = "</literal>";

    private static final String NEW_LINE = "\n";
    private static final String INDENT = "  ";  // two spaces

    private static final String XML_DECLARATION_TEMPLATE = "<?xml version=\"%s\" encoding=\"%s\"?>";

    private final StringBuilder builder;

    private boolean atStreamStart;

    private boolean omitXmlDeclaration;
    private String xmlVersion;
    private String xmlEncoding;

    private int indentationLevel;
    private boolean prettyPrint;

    private boolean oneRecordPerCollection;
    private boolean omitRootTag;

    public XmlEncoder() {
        this.builder = new StringBuilder();
        this.atStreamStart = true;

        this.omitXmlDeclaration = false;
        this.xmlVersion = "1.0";
        this.xmlEncoding = "UTF-8";

        this.indentationLevel = 0;
        this.prettyPrint = true;

        this.oneRecordPerCollection = false;
        this.omitRootTag = false;
    }

    /**
     * Starts and ends a collection with each record.
     * @param oneRecordPerCollection True, if each record is a collection.
     */
    public void emitOneRecordPerCollection(boolean oneRecordPerCollection)
    {
        this.oneRecordPerCollection = oneRecordPerCollection;
    }

    public void omitRootTag(boolean omitRootTag)
    {
        this.omitRootTag = omitRootTag;
    }

    public void omitXmlDeclaration(boolean omitXmlDeclaration)
    {
        this.omitXmlDeclaration = omitXmlDeclaration;
    }

    public void setXmlVersion(String xmlVersion)
    {
        this.xmlVersion = xmlVersion;
    }

    public void setXmlEncoding(String xmlEncoding)
    {
        this.xmlEncoding = xmlEncoding;
    }

    /**
     * Formats the resulting xml into a human readable form.
     * @param prettyPrint True, if formatting is activated.
     */
    public void setPrettyPrinting(boolean prettyPrint)
    {
        this.prettyPrint = prettyPrint;
    }

    @Override
    public void startRecord(final String identifier)
    {
        if (atStreamStart || oneRecordPerCollection)
        {
            if (!omitXmlDeclaration)
            {
                writeHeader();
                prettyPrintNewLine(builder);
            }
            if (!omitRootTag) {
                writeRaw(ROOT_OPEN, builder);
                prettyPrintNewLine(builder);
                incrementIndentationLevel();
            }
        }
        atStreamStart = false;

        prettyPrintIndentation(builder);
        writeRaw(String.format(RECORD_OPEN_TEMPLATE, escapeString(identifier)), builder);
        prettyPrintNewLine(builder);

        incrementIndentationLevel();
    }

    @Override
    public void endRecord()
    {
        decrementIndentationLevel();
        prettyPrintIndentation(builder);
        writeRaw(RECORD_CLOSE, builder);
        prettyPrintNewLine(builder);
        if (oneRecordPerCollection)
        {
            writeFooter();
        }
        sendAndClearData();
    }

    @Override
    public void startEntity(final String name)
    {
        prettyPrintIndentation(builder);
        writeRaw(String.format(ENTITIY_OPEN_TEMPLATE, escapeString(name)), builder);
        prettyPrintNewLine(builder);
        incrementIndentationLevel();
    }

    @Override
    public void endEntity()
    {
        decrementIndentationLevel();
        prettyPrintIndentation(builder);
        writeRaw(ENTITY_CLOSE, builder);
        prettyPrintNewLine(builder);
    }

    @Override
    public void literal(final String name, final String value)
    {
        prettyPrintIndentation(builder);
        writeRaw(String.format(LITERAL_OPEN_TEMPLATE, escapeString(name)), builder);
        writeEscaped(value, builder);
        writeRaw(LITERAL_CLOSE, builder);
        prettyPrintNewLine(builder);
    }

    @Override
    protected void onResetStream() {
        if (!atStreamStart) {
            writeFooter();
        }
        sendAndClearData();
        atStreamStart = true;
    }

    @Override
    protected void onCloseStream() {
        if (!oneRecordPerCollection) {
            writeFooter();
            sendAndClearData();
        }
    }


    /** Increments the indentation level by one */
    private void incrementIndentationLevel()
    {
        indentationLevel += 1;
    }

    /** Decrements the indentation level by one */
    private void decrementIndentationLevel()
    {
        indentationLevel -= 1;
    }

    /** Adds a XML Header */
    private void writeHeader()
    {
        writeRaw(String.format(XML_DECLARATION_TEMPLATE, xmlVersion, xmlEncoding), builder);
    }

    /** Closes the root tag */
    private void writeFooter()
    {
        if (!omitRootTag) writeRaw(ROOT_CLOSE, builder);
    }

    /** Write a unescaped sequence */
    private void writeRaw(final String str, StringBuilder builder)
    {
        builder.append(str);
    }

    /**
     * Escapes a string.
     * @param str A String.
     * @return A escaped string.
     */
    private String escapeString(final String str)
    {
        StringBuilder builder = new StringBuilder();
        writeEscaped(str, builder);
        return builder.toString();
    }

    /**
     * Escapes a string and appends it to a builder.
     * @param str A string.
     * @param builder A string builder.
     */
    private void writeEscaped(final String str, StringBuilder builder)
    {
        final int len = str.length();
        for (int i = 0; i < len; ++i) {
            final char c = str.charAt(i);
            final String entityName;
            switch (c) {
                case '&':
                    entityName = "amp";
                    break;
                case '<':
                    entityName = "lt";
                    break;
                case '>':
                    entityName = "gt";
                    break;
                case '\'':
                    entityName = "apos";
                    break;
                case '"':
                    entityName = "quot";
                    break;
                default:
                    entityName = null;
                    break;
            }

            if (entityName == null) {
                builder.append(c);
            } else {
                builder.append('&');
                builder.append(entityName);
                builder.append(';');
            }
        }
    }


    private void prettyPrintIndentation(StringBuilder builder)
    {
        if (prettyPrint)
        {
            String prefix = String.join("", Collections.nCopies(indentationLevel, INDENT));
            builder.append(prefix);
        }
    }

    private void prettyPrintNewLine(StringBuilder builder)
    {
        if (prettyPrint)
        {
            builder.append(NEW_LINE);
        }
    }

    private void sendAndClearData()
    {
        getReceiver().process(builder.toString());
        builder.delete(0, builder.length());
    }
}