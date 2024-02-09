/*
 * Copyright 2018-2023 Deutsche Nationalbibliothek et al
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

package org.metafacture.csv;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A csv encoder that converts a record into a csv line (Default separator: {@value #DEFAULT_SEP}).
 *
 * <p>
 * Each record represents a row. Each literal value represents a column value.
 * </P>
 *
 * @author eberhardtj (j.eberhardt@dnb.de)
 */
@Description("Encodes each value in a record as a csv row.")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("encode-csv")
public class CsvEncoder extends DefaultStreamPipe<ObjectReceiver<String>> {
    public static final char DEFAULT_SEP = CSVWriter.DEFAULT_SEPARATOR;
    private CSVWriter csvWriter;
    private StringWriter writer;
    private List<String> rowItems = new ArrayList<>();
    private boolean isFirstRecord = true;
    private List<String> header = new ArrayList<>();
    private char separator = DEFAULT_SEP;
    private boolean noQuotes;
    private boolean includeHeader;
    private boolean includeRecordId;

    /**
     * Creates an instance of {@link CsvEncoder} with a given separator.
     *
     * @param separator to separate columns
     */
    public CsvEncoder(final String separator) {
        this.separator = separator.charAt(0);
    }

    /**
     * Creates an instance of {@link CsvEncoder} with a given separator.
     *
     * @param separator to separate columns
     */
    public CsvEncoder(final char separator) {
        this.separator = separator;
    }

    /**
     * Creates an instance of {@link CsvEncoder}. The default separator is
     * {@value #DEFAULT_SEP}.
     */
    public CsvEncoder() {
    }

    /**
     * Start each line with the record ID.
     * Default is to not start each line with the record ID.
     *
     * @param includeRecordId true if the first column should consist of the record's ID
     */
    public void setIncludeRecordId(final boolean includeRecordId) {
        this.includeRecordId = includeRecordId;
    }

    /**
     * Add first record as a column description header.
     * Default is to not add a column description.
     *
     * @param includeHeader true if the first record should act as a CSV header, otherwise false
     */
    public void setIncludeHeader(final boolean includeHeader) {
        this.includeHeader = includeHeader;
    }

    /**
     * Set the character to separate the columns.
     * The default is {@value #DEFAULT_SEP}.
     *
     * @param separator set the character which separates the columns
     */
    public void setSeparator(final String separator) {
        if (separator.length() > 1) {
            throw new MetafactureException("Separator needs to be a single character.");
        }
        this.separator = separator.charAt(0);
    }

    /**
     * Set the character to separate the columns.
     * The default is {@value #DEFAULT_SEP}.
     *
     * @param separator set the character which separates the columns
     */
    public void setSeparator(final char separator) {
        this.separator = separator;
    }

    /**
     * Set if values should be not quoted by '"'.
     * The default is to quote values.
     *
     * @param noQuotes true if no quotes should be used. Default is false.
     */
    public void setNoQuotes(final boolean noQuotes) {
        this.noQuotes = noQuotes;
    }

    private void initialize() {
        writer = new StringWriter();
        final String emptyLineEnd = "";
        csvWriter = new CSVWriter(writer, separator, noQuotes ? CSVWriter.NO_QUOTE_CHARACTER : CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, emptyLineEnd);
    }

    private String[] arrayOf(final List<String> list) {
        final int length = list.size();
        return list.toArray(new String[length]);
    }

    private void resetCaches() {
        this.rowItems = new ArrayList<>();
    }

    private void writeRow(final List<String> rowItemsArray) {
        final String[] row = arrayOf(rowItemsArray);
        csvWriter.writeNext(row);
        final String line = writer.toString();
        getReceiver().process(line);
        writer.getBuffer().setLength(0);
    }

    @Override
    public void startRecord(final String identifier) {
        if (isFirstRecord) {
            initialize();
            if (includeRecordId) {
                header.add("record id");
            }
        }

        rowItems = new ArrayList<>();

        if (includeRecordId) {
            rowItems.add(identifier);
        }
    }

    @Override
    public void endRecord() {
        if (isFirstRecord) {
            if (includeHeader) {
                writeRow(header);
                header.clear();
            }
            isFirstRecord = false;
        }

        writeRow(rowItems);
        resetCaches();
    }

    @Override
    public void literal(final String name, final String value) {
        if (isFirstRecord) {
            header.add(name);
        }
        rowItems.add(value);
    }

    @Override
    public void onCloseStream() {
        try {
            csvWriter.close();
        }
        catch (final IOException e) {
            throw new MetafactureException(e);
        }
    }

    @Override
    public void onResetStream() {
        this.includeRecordId = false;
        this.includeHeader = false;
        this.header = new ArrayList<>();
        this.isFirstRecord = true;
        this.rowItems = new ArrayList<>();
    }

}
