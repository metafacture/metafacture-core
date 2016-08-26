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
package org.culturegraph.mf.stream.source;

import org.culturegraph.mf.util.ResourceUtil;
import org.culturegraph.mf.util.reflection.ObjectFactory;

/**
 * Builds {@link Opener}s
 *
 * @author Markus Michael Geipel
 *
 */
public final class OpenerFactory extends ObjectFactory<Opener> {
	public static final String POPERTIES_LOCATION = "metastream-openers.properties";

	public OpenerFactory() {
		super();
		loadClassesFromMap(ResourceUtil.loadProperties(POPERTIES_LOCATION), Opener.class);
	}
}
