/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.exceptions;


/**
 * Thrown if data processing failed because the data was not well-formed
 * according to its format definition. This exception should be thrown
 * if structural errors are encountered during processing. For example,
 * unbalanced opening and closing brackets in a format that uses
 * brackets to group data.
 *
 * @see WellformednessChecker
 * @see ValidationException
 *
 * @author Christoph Böhme
 *
 */
public final class WellformednessException extends FormatException {

	private static final long serialVersionUID = 3427046328020964145L;

	public WellformednessException(final String message) {
		super(message);
	}

	public WellformednessException(final Throwable cause) {
		super(cause);
	}

	public WellformednessException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
