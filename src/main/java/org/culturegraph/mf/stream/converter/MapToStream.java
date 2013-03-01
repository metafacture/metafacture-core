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
package org.culturegraph.mf.stream.converter;

import java.util.Map;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.util.StreamConstants;


/**
 * Creates a literal for each entry in a map.
 * 
 * @author Christoph Böhme
 */
public final class MapToStream extends
		DefaultObjectPipe<Map<?, ?>, StreamReceiver> {

	
	private Object idKey = StreamConstants.ID;
	
	public Object getIdKey() {
		return idKey;
	}
	
	public void setIdKey(final Object idKey) {
		this.idKey = idKey;
	}
	
	@Override
	public void process(final Map<?, ?> map) {
		final Object id = map.get(idKey);
		if (id == null) {
			getReceiver().startRecord(null);
		} else {
			getReceiver().startRecord(id.toString());
		}
		for (Map.Entry<?, ?> entry: map.entrySet()) {
			getReceiver().literal(entry.getKey().toString(), entry.getValue().toString());
		}
		getReceiver().endRecord();
	}
}
