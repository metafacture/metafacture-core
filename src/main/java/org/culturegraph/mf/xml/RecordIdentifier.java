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
package org.culturegraph.mf.xml;

/**
 * This interface declares methods for setting variables used by all record
 * sinks.
 *
 * @author dr0i
 *
 */
public interface RecordIdentifier {

	/**
	 * Sets the name property which will be used to create the name of the
	 * record of the sink. The value of this property should lead to a unique
	 * name because it will override existing ones.
	 *
	 * @param property
	 *            the property which will be used to extract a record name.
	 */
	public void setProperty(final String property);

}
