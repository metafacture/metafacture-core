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
package org.culturegraph.mf.morph.functions;


import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.util.ResourceUtil;
import org.culturegraph.mf.util.reflection.ObjectFactory;

/**
 * Provides the functions for {@link Metamorph}. By the default it contains the
 * standard function set. New functions can be registered during runtime.
 *
 * @author Markus Michael Geipel
 *
 */
public final class FunctionFactory extends ObjectFactory<Function> {

	public static final String POPERTIES_LOCATION = "morph-functions.properties";

	public FunctionFactory() {
		super();
		loadClassesFromMap(ResourceUtil.loadProperties(POPERTIES_LOCATION), Function.class);
	}

}
