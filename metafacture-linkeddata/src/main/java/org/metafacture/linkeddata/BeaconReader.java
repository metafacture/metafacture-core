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

package org.metafacture.linkeddata;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads BEACON format
 *
 * @author markus m geipel
 *
 */
@Description("Reads BEACON format")
@In(java.io.Reader.class)
@Out(StreamReceiver.class)
@FluxCommand("read-beacon")
public final class BeaconReader extends DefaultObjectPipe<java.io.Reader, StreamReceiver> {
    public static final String DEFAULT_RELATION = "seeAlso";

    private static final int MB = 1024 * 1024;
    private static final int BUFFER_SIZE = MB * 2;

    private static final int COLUMNS_EXTENDED_BEACON = 3;

    private static final String TARGET = "target";
    private static final Pattern ID_PATTERN = Pattern.compile("(\\{ID\\})|\\$PND");

    private int bufferSize = BUFFER_SIZE;
    private Pattern metaDataFilter = Pattern.compile(".*");
    private String relation = DEFAULT_RELATION;

    /**
     * Creates an instance of {@link BeaconReader}.
     */
    public BeaconReader() {
    }

    /**
     * Sets the buffer size.
     *
     * @param bufferSize in MB
     */
    public void setBufferSize(final int bufferSize) {
        this.bufferSize = MB * bufferSize;
    }

    /**
     * Sets the relation. Default value: {@value #DEFAULT_RELATION}.
     *
     * @param relation the relation
     */
    public void setRelation(final String relation) {
        this.relation = relation;
    }

    /**
     * Sets a metadata data filter. This is useful to filter out metadata.
     *
     * @param metaDataFilter the metadata data filter.
     */
    public void setMetaDataFilter(final String metaDataFilter) {
        this.metaDataFilter = Pattern.compile(metaDataFilter);
    }

    @Override // checkstyle-disable-line CyclomaticComplexity|ExecutableStatementCount
    public void process(final Reader reader) {
        final BufferedReader bReader = new BufferedReader(reader, bufferSize);
        final Map<String, String> institution = new HashMap<String, String>();
        final StreamReceiver receiver = getReceiver();
        String line;
        try {
            line = bReader.readLine();
            String target = null;
            while (line != null) {
                line = line.trim();
                if (!line.isEmpty()) {

                    if (line.charAt(0) == '#') {
                        final int splitPoint = line.indexOf(':');
                        if (splitPoint > 1 && splitPoint < line.length() - 1) {
                            final String key = line.substring(1, splitPoint).toLowerCase();
                            final String value = line.substring(splitPoint + 1).trim();
                            if (TARGET.equals(key)) {
                                target = value;
                            }
                            else if (metaDataFilter.matcher(key).find()) {
                                institution.put(key, value);
                            }
                        }
                    }
                    else {
                        final String[] parts = line.split("\\|");
                        final String url;
                        if (parts.length == COLUMNS_EXTENDED_BEACON) {
                            url = parts[2];
                        }
                        else {
                            if (target == null || target.isEmpty()) {
                                throw new MetafactureException("Error in BEACON file: target missing");
                            }
                            final Matcher matcher = ID_PATTERN.matcher(target);
                            url = matcher.replaceFirst(parts[0]);
                        }
                        receiver.startRecord(parts[0]);
                        receiver.startEntity(relation);
                        receiver.literal("url", url);
                        for (final Map.Entry<String, String> instEntry : institution.entrySet()) {
                            receiver.literal(instEntry.getKey(), instEntry.getValue());
                        }
                        receiver.endEntity();
                        receiver.endRecord();
                    }
                }
                line = bReader.readLine();
            }
            bReader.close();

        }
        catch (final IOException e) {
            throw new MetafactureException("Error reading BEACON format", e);
        }
    }

}
