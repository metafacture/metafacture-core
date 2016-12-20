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
package org.culturegraph.mf.metamorph.api;

/**
 * Describes a location in a Metamorph script. Instances of this interface are
 * returned by {@link KnowsSourceLocation}.
 *
 * @author Christoph Böhme
 */
public interface SourceLocation {

	String getFileName();

	Position getStartPosition();

	Position getEndPosition();

	/**
	 * Describes a position in a file by line and column number.
	 *
	 * @author Christoph Böhme
	 */
	interface Position {

		int getLineNumber();

		int getColumnNumber();

	}

}
