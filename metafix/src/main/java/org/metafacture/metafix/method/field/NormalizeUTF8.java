/*
 * Copyright 2025 hbz NRW
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

package org.metafacture.metafix.method.field;

import org.metafacture.metafix.FixCommand;
import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.api.FixFunction;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;
import java.util.Map;

/**
 * Performs normalization of diacritics in UTF-8 encoded strings.
 *
 * @author Tobias Bülte, hbz
 */
@FixCommand("normalize_utf8")
public class NormalizeUTF8 implements FixFunction {

    /**
     * Creates an instance of {@link NormalizeUTF8}.
     */
    public NormalizeUTF8() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        record.transform(params.get(0), s -> Normalizer.normalize(s, Form.NFC));
    }

}
