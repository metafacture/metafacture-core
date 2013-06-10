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

import java.util.SortedSet;
import java.util.TreeSet;

import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.morph.NamedValueSource;


/**
 * Corresponds to the <code>&lt;range&gt;</code> tag.
 *
 * @author Christoph Böhme
 */
public final class Range extends AbstractCollect {
	private final SortedSet<Integer> values = new TreeSet<Integer>();

	private Integer first;

	public Range(final Metamorph metamorph) {
		super(metamorph);
		setNamedValueReceiver(metamorph);
	}

	@Override
	protected void emit() {
		for (final Integer i: values) {
			getNamedValueReceiver().receive(getName(), i.toString(), this, getRecordCount(), getEntityCount());
		}
	}

	@Override
	protected boolean isComplete() {
		return false;
	}

	@Override
	protected void receive(final String name, final String value, final NamedValueSource source) {
		if (first == null) {
			first = Integer.valueOf(value);
		} else {
			final int last = Integer.valueOf(value).intValue();
			for (int i = first.intValue(); i <= last; ++i) {
				values.add(Integer.valueOf(i));
			}
			first = null;
		}
	}

	@Override
	protected void clear() {
		values.clear();
		first = null;
	}

}
