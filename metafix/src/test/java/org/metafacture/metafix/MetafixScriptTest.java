/*
 * Copyright 2021 hbz NRW
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

package org.metafacture.metafix;

import org.metafacture.framework.helpers.DefaultStreamReceiver;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.function.Consumer;

/**
 * Tests Metafix script level methods.
 */
public class MetafixScriptTest {

    private static final String MAP_NAME = "testMap";

    private static final String CSV_MAP = "src/test/resources/org/metafacture/metafix/maps/test.csv";
    private static final String TSV_MAP = "src/test/resources/org/metafacture/metafix/maps/test.tsv";

    public MetafixScriptTest() {
    }

    @Test
    public void shouldPutEmptyInternalMap() {
        assertMap("put_map('" + MAP_NAME + "')", MAP_NAME);
    }

    @Test
    public void shouldPutMultipleInternalMaps() {
        assertFix("put_map('" + MAP_NAME + "')\nput_map('" + MAP_NAME + "2')", f -> {
            assertMap(f, MAP_NAME);
            assertMap(f, MAP_NAME + "2");
        });
    }

    @Test
    public void shouldPutInternalMapWithOptions() {
        assertMap("put_map('" + MAP_NAME + "', k1: 'v1', k2: 'v2')", MAP_NAME);
    }

    @Test
    public void shouldPutExternalFileMap() {
        assertMap("put_filemap('" + CSV_MAP + "')", CSV_MAP);
    }

    @Test
    public void shouldPutMultipleExternalFileMaps() {
        assertFix("put_filemap('" + CSV_MAP + "')\nput_filemap('" + TSV_MAP + "')", f -> {
            assertMap(f, CSV_MAP);
            assertMap(f, TSV_MAP);
        });
    }

    @Test
    public void shouldPutExternalFileMapWithName() {
        assertMap("put_filemap('" + CSV_MAP + "', '" + MAP_NAME + "')", MAP_NAME);
    }

    @Test
    public void shouldPutExternalFileMapWithOptions() {
        assertMap("put_filemap('" + TSV_MAP + "', sep_char: '\t')", TSV_MAP);
    }

    @Test
    public void shouldPutExternalFileMapWithNameAndOptions() {
        assertMap("put_filemap('" + TSV_MAP + "', '" + MAP_NAME + "', sep_char: '\t')", MAP_NAME);
    }

    private void assertMap(final String fixDef, final String mapName) {
        assertFix(fixDef, f -> assertMap(f, mapName));
    }

    private void assertMap(final Metafix metafix, final String mapName) {
        Assertions.assertTrue(metafix.getMapNames().contains(mapName));
        Assertions.assertNotNull(metafix.getMap(mapName));
    }

    private void assertFix(final String fixDef, final Consumer<Metafix> consumer) {
        try {
            final Metafix metafix = new Metafix(fixDef);

            // Prepare and trigger script execution
            metafix.setReceiver(new DefaultStreamReceiver());
            metafix.startRecord("");
            metafix.endRecord();

            consumer.accept(metafix);
        }
        catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
