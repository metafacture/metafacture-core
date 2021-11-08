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

package org.metafacture.csv;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * Decodes lines of CSV files. First line may be interpreted as header.
 *
 * @author Markus Michael Geipel
 * @author Fabian Steeg (fsteeg)
 *
 */
@Description("Decodes lines of CSV files. First line may be interpreted as header.")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-csv")
public final class CsvDecoder extends DefaultObjectPipe<String, StreamReceiver>  {

    public static final char DEFAULT_SEP = ',';
    private char separator = DEFAULT_SEP;

    private String[] header = new String[0];
    private int count;
    private boolean hasHeader;

    /**
     * Creates an instance of {@link CsvDecoder} with a given separator.
     *
     * @param separator to split lines
     */
    public CsvDecoder(final String separator) {
        this.separator = separator.charAt(0);
    }

    /**
     * Creates an instance of {@link CsvDecoder} with a given separator.
     *
     * @param separator to split lines
     */
    public CsvDecoder(final char separator) {
        this.separator = separator;
    }

    /**
     * Creates an instance of {@link CsvDecoder}. The default separator is
     * {@value #DEFAULT_SEP}.
     */
    public CsvDecoder() {
    }

    @Override
    public void process(final String string) {
        assert !isClosed();
        final String[] parts = parseCsv(string);
        if (hasHeader) {
            if (header.length == 0) {
                header = parts;
            }
            else if (parts.length == header.length) {
                getReceiver().startRecord(String.valueOf(++count));
                for (int i = 0; i < parts.length; ++i) {
                    getReceiver().literal(header[i], parts[i]);
                }
                getReceiver().endRecord();
            }
            else {
                throw new IllegalArgumentException(
                        String.format(
                                "wrong number of columns (expected %s, was %s) in input line: %s",
                                header.length, parts.length, string));
            }
        }
        else {
            getReceiver().startRecord(String.valueOf(++count));
            for (int i = 0; i < parts.length; ++i) {
                getReceiver().literal(String.valueOf(i), parts[i]);
            }
            getReceiver().endRecord();
        }
    }

    private String[] parseCsv(final String string) {
        String[] parts = new String[0];
        try {
            final CSVReader reader = new CSVReader(new StringReader(string),
                    separator);
            final List<String[]> lines = reader.readAll();
            if (lines.size() > 0) {
                parts = lines.get(0);
            }
            reader.close();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        return parts;
    }

    /**
     * Flags if the CSV has a header or comes without a header.
     *
     * @param hasHeader true if the CSV has a header, otherwise false
     */
    public void setHasHeader(final boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    /**
     * Sets the separator.
     *
     * @param separator the separator as a String. The first character is used as
     *                  the separator.
     */
    public void setSeparator(final String separator) {
        this.separator = separator.charAt(0);
    }
}
