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
package org.culturegraph.mf.metamorph.functions;

import org.culturegraph.mf.commons.tries.SetReplacer;
import org.culturegraph.mf.metamorph.api.helpers.AbstractSimpleStatelessFunction;

/**
 * @author Markus Michael Geipel
 */
public final class SetReplace extends AbstractSimpleStatelessFunction {

	private final SetReplacer setReplacer = new SetReplacer();
	private boolean prepared;

	@Override
	public String process(final String text) {
		if (!prepared) {
			setReplacer.addReplacements(getMap());
			prepared = true;
		}
		return setReplacer.replaceIn(text);

	}

}
