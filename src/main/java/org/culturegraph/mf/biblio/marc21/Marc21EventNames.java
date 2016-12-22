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

/**
 * Event names used by MARC 21 encoders and decoders.
 *
 * @author Christoph Böhme
 */
public final class Marc21EventNames {

  /**
   * Name of the <i>literal</i> event which contains the value of the
   * <i>type</i> attribute of the <i>record</i> element.
   */
  public static final String MARCXML_TYPE_LITERAL = "type";

  /**
   * Name of the <i>entity</i> event which contains the bibliographic
   * information in the record leader.
   *
   * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
   * Standard: Record Leader</a>
   */
  public static final String LEADER_ENTITY = "leader";

  /**
   * Name of the <i>literal</i> event emitted for the record status field.
   * <p>
   * The name of the literal is &quot;{@value #RECORD_STATUS_LITERAL}&quot;.
   * <p>
   * The record status is specified at position 5 in the record leader.
   *
   * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
   * Standard: Record Leader</a>
   */
  public static final String RECORD_STATUS_LITERAL = "status";

  /**
   * Name of the <i>literal</i> event emitted for the bibliographic level
   * field.
   * <p>
   * The name of the literal is
   * &quot;{@value #BIBLIOGRAPHIC_LEVEL_LITERAL}&quot;.
   * <p>
   * The bibliographic level is specified at position 7 in the record leader.
   *
   * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
   * Standard: Record Leader</a>
   */
  public static final String BIBLIOGRAPHIC_LEVEL_LITERAL = "bibliographicLevel";

  /**
   * Name of the <i>literal</i> event emitted for the type of control field.
   * <p>
   * The name of the literal is &quot;{@value #TYPE_OF_CONTROL_LITERAL}&quot;.
   * <p>
   * The type of control is specified at position 8 in the record leader.
   *
   * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
   * Standard: Record Leader</a>
   */
  public static final String TYPE_OF_CONTROL_LITERAL = "typeOfControl";

  /**
   * Name of the <i>literal</i> event emitted for the character coding scheme
   * field.
   * <p>
   * The name of the literal is &quot;{@value #CHARACTER_CODING_LITERAL}&quot;.
   * <p>
   * The character coding scheme is specified at position 9 in the record
   * leader.
   *
   * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
   * Standard: Record Leader</a>
   */
  public static final String CHARACTER_CODING_LITERAL = "characterCodingScheme";

  /**
   * Name of the <i>literal</i> event emitted for the encoding level field.
   * <p>
   * The name of the literal is &quot;{@value #ENCODING_LEVEL_LITERAL}&quot;.
   * <p>
   * The encoding level is specified at position 17 in the record leader.
   *
   * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
   * Standard: Record Leader</a>
   */
  public static final String ENCODING_LEVEL_LITERAL = "encodingLevel";

  /**
   * Name of the <i>literal</i> event emitted for the descriptive cataloging
   * form field.
   * <p>
   * The name of the literal is &quot;{@value #CATALOGING_FORM_LITERAL}&quot;.
   * <p>
   * The descriptive cataloging form is specified at position 18 in the record
   * leader.
   *
   * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
   * Standard: Record Leader</a>
   */
  public static final String CATALOGING_FORM_LITERAL = "catalogingForm";

  /**
   * Name of the <i>literal</i> event emitted for the multipart resource
   * record level field.
   * <p>
   * The name of the literal is &quot;{@value #MULTIPART_LEVEL_LITERAL}&quot;.
   * <p>
   * The multipart resource record level is specified at position 19 in the
   * record leader.
   *
   * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
   * Standard: Record Leader</a>
   */
  public static final String MULTIPART_LEVEL_LITERAL = "multipartLevel";

  /**
   * Name of the <i>literal</i> event emitted for the type of record field.
   * <p>
   * The name of the literal is &quot;{@value #RECORD_TYPE_LITERAL}&quot;.
   * <p>
   * The type of record is specified at position 6 in the record leader.
   *
   * @see <a href="http://www.loc.gov/marc/bibliographic/bdleader.html">MARC 21
   * Standard: Record Leader</a>
   */
  public static final String RECORD_TYPE_LITERAL = "type";

  private Marc21EventNames() {
    throw new AssertionError("class should not be instantiated");
  }

}
