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
import org.metafacture.metamorph.functions.URLEncode;

import java.util.List;
import java.util.Map;

@FixCommand("uri_encode")
public class UriEncode implements FixFunction {

    /**
     * Creates an instance of {@link UriEncode}.
     */
    public UriEncode() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final URLEncode urlEncoder = new URLEncode();
        withOption(options, "safe_chars", urlEncoder::setSafeChars);
        withOption(options, "plus_for_space", urlEncoder::setPlusForSpace, this::getBoolean);

        record.transform(params.get(0), urlEncoder::process);
    }

}
