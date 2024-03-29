/*
 *  Copyright 2015 Lars G. Svensson
 *
 *  Licensed under the Apache License, Version 2.0 the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.metafacture.biblio;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Parses a raw Aseq stream. UTF-8 encoding expected.
 *
 * @author Lars G. Svensson
 */
@Description("Parses a raw Aseq record (UTF-8 encoding expected).")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-aseq")
public final class AseqDecoder extends DefaultObjectPipe<String, StreamReceiver> {

    private static final String FIELD_DELIMITER = "\n";

    private static final int CATEGORY_BEGIN = 10;
    private static final int CATEGORY_END = 15;
    private static final int FIELD_CONTENT_BEGIN = 18;
    private static final int RECORD_IDENTIFIER_BEGIN = 0;
    private static final int RECORD_IDENTIFIER_END = 9;

    /**
     * Creates an instance of {@link AseqDecoder}.
     */
    public AseqDecoder() {
    }

    @Override
    public void process(final String record) {
        assert !isClosed();
        final String trimedRecord = record.trim();
        if (trimedRecord.isEmpty()) {
            return;
        }
        final String[] lines = trimedRecord.split(FIELD_DELIMITER);
        for (int i = 0; i < lines.length; ++i) {
            final String field = lines[i];
            if (i == 0) {
                getReceiver().startRecord(field.substring(RECORD_IDENTIFIER_BEGIN, RECORD_IDENTIFIER_END));
            }
            final String category = field.substring(CATEGORY_BEGIN, CATEGORY_END).trim();
            final String fieldContent = field.substring(FIELD_CONTENT_BEGIN).trim();
            if (!fieldContent.startsWith("$$")) {
                getReceiver().literal(category, fieldContent);
            }
            else {
                getReceiver().startEntity(category);
                final String[] subfields = fieldContent.split("\\$\\$");
                for (final String subfield : subfields) {
                    if (!subfield.isEmpty()) {
                        getReceiver().literal(subfield.substring(0, 1), subfield.substring(1));
                    }
                }
                getReceiver().endEntity();
            }
        }
        getReceiver().endRecord();
    }

}
