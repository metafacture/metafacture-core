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
package org.culturegraph.mf.flux;

import org.culturegraph.mf.framework.MetafactureException;

/**
 * @author Markus Michael Geipel
 *
 */
public final class FluxParseException extends MetafactureException {

	private static final long serialVersionUID = -5728526458760884738L;

	public FluxParseException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public FluxParseException(final String message) {
		super(message);
	}

}
