/**
 * 
 */
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
package org.culturegraph.mf.morph.functions;

import org.culturegraph.mf.morph.EntityEndIndicator;
import org.culturegraph.mf.morph.EntityEndListener;

/**
 * @author Markus Michael Geipel
 * 
 */
public final class IfNotReceived extends AbstractStatefulFunction implements EntityEndListener {

	private boolean receivedData;
	private String value;
	private String name;
	private String inEntity = EntityEndIndicator.RECORD_KEYWORD;

	@Override
	public String process(final String value) {
		receivedData = true;
		return null;
	}

	public void setIn(final String inEntity) {
		this.inEntity = inEntity;
	}
	
	public void setValue(final String value) {
		this.value = value;
	}
	
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	protected void reset() {
		receivedData = false;
	}

	@Override
	protected boolean doResetOnEntityChange() {
		return false;
	}

	@Override
	public void setEntityEndIndicator(final EntityEndIndicator indicator) {
		indicator.addEntityEndListener(this, inEntity);
	}

	@Override
	public void onEntityEnd(final String entityName, final int recordCount, final int entityCount) {
		if (receivedData) {
			receivedData = false;
			return;
		}
		getNamedValueReceiver()
				.receive(name, value, getNamedValueSource(), getRecordCount(), 0);
	}
}
