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
package org.culturegraph.mf.morph.collectors;


import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.morph.MorphBuilder;
import org.culturegraph.mf.util.ResourceUtil;
import org.culturegraph.mf.util.reflection.ObjectFactory;

/**
 * Factory for all maps used by {@link Metamorph} and {@link MorphBuilder}
 * 
 * @author Markus Michael Geipel
 *
 */
public final class CollectFactory extends ObjectFactory<Collect> {
	public static final String POPERTIES_LOCATION = "morph-collectors.properties";

	public CollectFactory() {
		super();
		loadClassesFromMap(ResourceUtil.loadProperties(POPERTIES_LOCATION), Collect.class);
	}
}
