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

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.morph.Metamorph;
import org.culturegraph.mf.morph.NamedValueSource;
import org.culturegraph.mf.util.StringUtil;



/**
 * Corresponds to the <code>&lt;choose&gt;</code> tag.
 * 
 * @author Christoph Böhme <c.boehme@dnb.de>,  Markus Michael Geipel
 *
 */
public final class Choose extends AbstractCollect{

	private String value;
	private String name;
	private int priority = Integer.MAX_VALUE;
	private final Map<NamedValueSource, Integer> priorities = new HashMap<NamedValueSource, Integer>();
	private int nextPriority;
//	private String defaultValue;

	
	/**
	 * @param metamorph
	 */
	public Choose(final Metamorph metamorph) {
		super(metamorph);
		setNamedValueReceiver(metamorph);
	}
	
	//public void setDefaultValue(final String defaultValue) {
	//	this.defaultValue = defaultValue;
//	}


	@Override
	protected void emit() {
		if(!isEmpty()){
			getNamedValueReceiver().receive(StringUtil.fallback(getName(), name), StringUtil.fallback(getValue(), value), this, getRecordCount(), getEntityCount());
		}
		clear();
	}


	private boolean isEmpty() {
		return name==null;
	}


	@Override
	protected boolean isComplete() {
		return false;
	}


	@Override
	protected void clear() {
		value = null;
		name = null;
		priority = Integer.MAX_VALUE;
	}


	@Override
	protected void receive(final String name, final String value, final NamedValueSource source) {
		final int sourcePriority = priorities.get(source).intValue();
		 
		if (sourcePriority <= priority) {
			this.value = value;
			this.name = name;
			this.priority = sourcePriority;
		}
	}
	
	@Override
	public void onNamedValueSourceAdded(final NamedValueSource namedValueSource) {
		priorities.put(namedValueSource, Integer.valueOf(nextPriority));
		nextPriority += 1;
	}
}
