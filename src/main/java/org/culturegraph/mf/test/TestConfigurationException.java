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
package org.culturegraph.mf.test;

/**
 * @author Christoph Böhme <c.boehme@dnb.de>
 *
 */
public final class TestConfigurationException extends RuntimeException {

	private static final long serialVersionUID = -4980848442374614262L;

	/**
	 * @param message
	 */
	TestConfigurationException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	TestConfigurationException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	TestConfigurationException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
