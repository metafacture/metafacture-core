/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.biblio.marc21;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.culturegraph.mf.biblio.iso2709.RecordFormat;

/**
 * Useful constants for the MARC 21 format.
 *
 * @author Christoph Böhme
 */
final class Marc21Constants {

  static final RecordFormat MARC21_FORMAT = RecordFormat.create()
      .withIndicatorLength(2)
      .withIdentifierLength(2)
      .withFieldLengthLength(4)
      .withFieldStartLength(5)
      .withImplDefinedPartLength(0)
      .build();

  static final Charset MARC21_CHARSET = StandardCharsets.UTF_8;

  static final int RECORD_TYPE_INDEX = 0;
  static final int BIBLIOGRAPHIC_LEVEL_INDEX = 1;
  static final int TYPE_OF_CONTROL_INDEX = 2;
  static final int CHARACTER_CODING_INDEX = 3;
  static final int ENCODING_LEVEL_INDEX = 0;
  static final int CATALOGING_FORM_INDEX = 1;
  static final int MULTIPART_LEVEL_INDEX = 2;

  static final char[] RECORD_STATUS_CODES = { 'a', 'c', 'd', 'n', 'p' };
  static final char[] RECORD_TYPE_CODES = {
    'a', 'c', 'd', 'e', 'f', 'g', 'i', 'j', 'k', 'm', 'o', 'p', 'r', 't'
  };
  static final char[] BIBLIOGRAPHIC_LEVEL_CODES = {
    'a', 'b', 'c', 'd', 'i', 'm', 's'
  };
  static final char[] TYPE_OF_CONTROL_CODES = { ' ', 'a' };
  static final char[] CHARACTER_CODING_CODES = { 'a' };
  static final char[] ENCODING_LEVEL_CODES = {
    ' ', '1', '2', '3', '4', '5', '7', '8', 'u', 'z'
  };
  static final char[] CATALOGING_FORM_CODES = { ' ', 'a', 'c', 'i', 'u' };
  static final char[] MULTIPART_LEVEL_CODES = { ' ', 'a', 'b', 'c' };

  static final char RESERVED_CHAR = '0';

  private Marc21Constants() {
    throw new AssertionError("class should not be instantiated");
  }

}
