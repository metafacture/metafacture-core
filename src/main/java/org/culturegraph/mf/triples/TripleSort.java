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
package org.culturegraph.mf.triples;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.objects.Triple;

/**
 * Sorts triples
 * @author markus geipel
 *
 */
@Description("Sorts triples")
@In(Triple.class)
@Out(Triple.class)
@FluxCommand("sort-triples")
public final class TripleSort extends AbstractTripleSort {

	@Override
	protected void sortedTriple(final Triple triple) {
		getReceiver().process(triple);
	}

	public void setBy(final Compare compare){
		setCompare(compare);
	}

	public void setOrder(final Order order){
		setSortOrder(order);
	}


}
