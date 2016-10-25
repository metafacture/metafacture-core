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
package org.culturegraph.mf.framework;

/**
 * Constants for commonly used literal and entity names.
 *
 * @author Christoph Böhme
 */
public final class StandardEventNames {

	/**
	 * Name of the literal holding the record id. The literal's value should
	 * either be the same as the record id in the <i>start-record</i> event or be
	 * intended to replace the current record id in a subsequent processing step.
	 */
	public static final String ID = "_id";

	private StandardEventNames() {
		throw new AssertionError("no instances allowed");
	}

}
