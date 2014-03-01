/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.types;

/**
 * Constants used by the CGEntity file format
 * 
 * @author Markus Michael Geipel
 *
 * @see CGEntityDecoder
 * @see CGEntityEncoder
 * 
 * @deprecated Use FormetaDecoder and FormetaEncoder instead
 * 
 */
@Deprecated
public final class CGEntity {
	public static final char FIELD_DELIMITER = '\u001e';
	public static final char SUB_DELIMITER = '\u001f';
	public static final char NEWLINE_ESC = '\u001d';
	public static final char NEWLINE = '\n';
	public static final char LITERAL_MARKER = '-';
	public static final char ENTITY_START_MARKER = '<';
	public static final char ENTITY_END_MARKER = '>';
	
	private CGEntity() {
		// No instances allowed
	}
}
