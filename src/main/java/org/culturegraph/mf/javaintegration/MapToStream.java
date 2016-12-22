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
package org.culturegraph.mf.javaintegration;

import java.util.Map;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.StandardEventNames;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

/**
 * Emits a {@link Map} as a record with a literal for each entry in the map.
 * When receiving an empty map only a <i>start-record</i> and <i>end-record</i>
 * event is emitted.
 * <p>
 * If the map contains an entry whose key value matches the one set as
 * {@link #setIdKey(Object)}, this entry's value is used as the record id.
 * <p>
 * The keys and values in the map can be of any type. They will be converted to
 * strings using their {@link Object#toString()} method. Neither key nor value
 * may be null.
 * <p>
 * The order in which the literals are emitted is unspecified.
 * <p>
 * For example, when receiving the following map with string keys and integer
 * values:
 * <pre>{@literal
 * final Map<String, Integer> map = new HashMap<>();
 * map.put("_id", 1);
 * map.put("lit-A", 2);
 * map.put("lit-B", 3);
 * }</pre>
 * Then this module will emit the following sequence of events:
 * <pre>{@literal
 * start-record "1"
 * literal "lit-A": 2
 * literal "lit-B": 3
 * end-record
 * }</pre>
 *
 * @author Christoph Böhme
 */
@In(Map.class)
@Out(StreamReceiver.class)
@FluxCommand("map-to-stream")
public final class MapToStream extends
		DefaultObjectPipe<Map<?, ?>, StreamReceiver> {

	private Object idKey = StandardEventNames.ID;

	/**
	 * Sets the key of the map entry that is used for the record id.
	 * <p>
	 * The default id key is &quot;{@value StandardEventNames#ID}&quot;.
	 * <p>
	 * This parameter can be changed anytime during processing. The new value
	 * becomes effective with the next record being processed.
	 *
	 * @param idKey the id key. The object passed here is used in a call to
	 * {@link Map#get(Object)} to get the identifier value.
	 */
	public void setIdKey(final Object idKey) {
		this.idKey = idKey;
	}

	public Object getIdKey() {
		return idKey;
	}

	@Override
	public void process(final Map<?, ?> map) {
		final Object id = map.get(idKey);
		if (id == null) {
			getReceiver().startRecord("");
		} else {
			getReceiver().startRecord(id.toString());
		}
		for (final Map.Entry<?, ?> entry: map.entrySet()) {
			getReceiver().literal(entry.getKey().toString(),
					entry.getValue().toString());
		}
		getReceiver().endRecord();
	}

}
