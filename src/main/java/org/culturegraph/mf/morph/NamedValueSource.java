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
package org.culturegraph.mf.morph;

/**
 * Base interface for all classes in {@link Metamorph} which emit
 * name-value-pairs.
 *
 * @author Markus Michael Geipel
 * @author Christoph Böhme
 *
 */
public interface NamedValueSource extends KnowsSourceLocation {

	/**
	 * Connects a source of named values to a receiver of named values.
	 *
	 * Users should not call this method to connect sources and
	 * receivers but rather call
	 * {@link NamedValueReceiver.addNamedValueSource}.
	 *
	 * @param receiver
	 * @return reference to receiver
	 */
	<R extends NamedValueReceiver> R setNamedValueReceiver(R receiver);

}
