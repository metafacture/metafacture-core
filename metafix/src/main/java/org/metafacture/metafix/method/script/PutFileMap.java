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

package org.metafacture.metafix.method.script;

import org.metafacture.metafix.FixCommand;
import org.metafacture.metafix.Metafix;
import org.metafacture.metafix.Record;
import org.metafacture.metafix.api.FixFunction;
import org.metafacture.metamorph.maps.FileMap;

import java.util.List;
import java.util.Map;

@FixCommand("put_filemap")
public class PutFileMap implements FixFunction {

    private static final String FILEMAP_SEPARATOR_OPTION = "sep_char";
    private static final String FILEMAP_DEFAULT_SEPARATOR = ",";

    /**
     * Creates an instance of {@link PutFileMap}.
     */
    public PutFileMap() {
    }

    @Override
    public void apply(final Metafix metafix, final Record record, final List<String> params, final Map<String, String> options) {
        final String fileName = params.get(0);
        final FileMap fileMap = new FileMap();

        fileMap.setSeparator(options.getOrDefault(FILEMAP_SEPARATOR_OPTION, FILEMAP_DEFAULT_SEPARATOR));
        fileMap.setFile(metafix.resolvePath(fileName));

        withOption(options, "allow_empty_values", fileMap::setAllowEmptyValues, this::getBoolean);
        withOption(options, "compression", fileMap::setCompression);
        withOption(options, "decompress_concatenated", fileMap::setDecompressConcatenated, this::getBoolean);
        withOption(options, "encoding", fileMap::setEncoding);
        withOption(options, "expected_columns", fileMap::setExpectedColumns, this::getInteger);
        withOption(options, "ignore_pattern", fileMap::setIgnorePattern);
        withOption(options, "key_column", fileMap::setKeyColumn, this::getInteger);
        withOption(options, "value_column", fileMap::setValueColumn, this::getInteger);

        metafix.putMap(params.size() > 1 ? params.get(1) : fileName, fileMap);
    }

}
