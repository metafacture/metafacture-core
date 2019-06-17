/* Copyright 2016,2019 Christoph Böhme and hbz
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

/**
 * Useful constants for PICA+.
 * PICA+ comes with two possible serializations:
 * a normalized one and a non-normalized.
 *
 * @author Christoph Böhme
 * @author Pascal Christoph (dr0i)
 *
 */

package org.metafacture.biblio.pica;

final class PicaConstants{
    public static char RECORD_MARKER = '\u001d';
    public static char FIELD_MARKER = '\u001e';
    public static char SUBFIELD_MARKER = '\u001f';
    public static char FIELD_END_MARKER = '\n';

    public static void setNormalizedSerialization() {
        RECORD_MARKER = '\u001d';
        FIELD_MARKER = '\u001e';
        SUBFIELD_MARKER = '\u001f';
        FIELD_END_MARKER = '\n';
    }

    public static void setNonNormalizedSerialization() {
        RECORD_MARKER = '\n';
        FIELD_MARKER = '\n'; //this is a dummy
        SUBFIELD_MARKER = '$';
        FIELD_END_MARKER = '\n';
    }

    private PicaConstants() {
        // No instances allowed
    }

}
