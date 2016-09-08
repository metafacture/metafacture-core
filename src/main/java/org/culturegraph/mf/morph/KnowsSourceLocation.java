/*
 * Copyright 2014 Christoph Böhme
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
package org.culturegraph.mf.morph;

import org.culturegraph.mf.util.xml.Location;

/**
 * Metamorph elements which know about their location in the
 * morph definition file should implement this interface to
 * make the information about their location available in a
 * standardised way.
 *
 * @author Christoph Böhme
 *
 */
public interface KnowsSourceLocation {

	/**
	 * Sets the {@link Location} object which describes
	 * where in the morph definition file the implementing
	 * object is defined.
	 *
	 * @param sourceLocation a source location
	 */
	void setSourceLocation(final Location sourceLocation);

	/**
	 * @return a {@link Location} object for the
	 * 		location in the morph definition file
	 * 		where the implementing object instance
	 * 		is defined.
	 */
	Location getSourceLocation();

}
