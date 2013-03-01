/*
 *  Copyright 2013 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.stream;

/**
 * @author Markus Michael Geipel
 */
public final class DataFilePath {
	
	public static final String DATA_PREFIX = "data/";
//	public static final String MORPH_PREFIX = "morph/";
	
	public static final String TITLE_MAB = DATA_PREFIX + "TITLE_10entries.mab2";
	public static final String TITLE_PICA = DATA_PREFIX + "TITLE_10entries.pica";
	public static final String PND_PICA = DATA_PREFIX + "PND_10entries.pica";
	public static final String TITLE_MARC = DATA_PREFIX + "TITLE_10entries.marc21";
	
//	public static final String SYTAX_ERROR_MM = MORPH_PREFIX + "error.xml";
//	public static final String TITLE_MAB_MM =  MORPH_PREFIX + "title_mab.xml";
	
	public static final String CG_XML = DATA_PREFIX + "cgxml_test.xml";
	
	public static final String GENERIC_XML = DATA_PREFIX + "generic_xml_test.xml";
	
	private DataFilePath() {/*no instances exist*/}
	

}
