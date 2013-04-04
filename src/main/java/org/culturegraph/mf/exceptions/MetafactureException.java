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
package org.culturegraph.mf.exceptions;

/**
 * Base class for exceptions thrown by Metastream.
 * 
 * @author Christoph Böhme <c.boehme@dnb.de>
 */
public class MetafactureException extends RuntimeException {

	private static final long serialVersionUID = -2953264524472071347L;

	public MetafactureException(final String message) {
		super(message);
	}

	public MetafactureException(final Throwable cause) {
		super(cause);
	}

	public MetafactureException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
