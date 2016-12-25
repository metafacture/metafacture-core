/*
 *  Copyright 2014-2016 hbz, Fabian Steeg
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
package org.culturegraph.mf.elasticsearch;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Add Elasticsearch bulk indexing metadata to JSON input.
 *
 * @author Fabian Steeg (fsteeg)
 * @author Jens Wille
 *
 */
@In(String.class)
@Out(String.class)
@FluxCommand("json-to-elasticsearch-bulk")
public class JsonToElasticsearchBulk extends
		DefaultObjectPipe<String, ObjectReceiver<String>> {

	/**
	 * Use a MultiMap with Jackson to collect values from multiple fields with
	 * identical names under a single key.
	 */
	static class MultiMap extends HashMap<String, Object> {
		private static final long serialVersionUID = 490682490432334605L;

		MultiMap() {
			// default constructor for Jackson
		}

		@Override
		public Object put(String key, Object value) {
			if (containsKey(key)) {
				Object oldValue = get(key);
				if (oldValue instanceof Set) {
					@SuppressWarnings("unchecked")
					Set<Object> vals = ((Set<Object>) oldValue);
					vals.add(value);
					return super.put(key, vals);
				}
				HashSet<Object> set = new HashSet<>(Arrays.asList(oldValue, value));
				return super.put(key, set.size() == 1 ? value : set);
			}
			return super.put(key, value);
		}
	}

	private ObjectMapper mapper = new ObjectMapper();
	private String[] idPath;
	private String type;
	private String index;

	/**
	 * @param idPath The key path of the JSON value to be used as the ID for the record
	 * @param type The Elasticsearch index type
	 * @param index The Elasticsearch index name
	 */
	public JsonToElasticsearchBulk(String[] idPath, String type, String index) {
		this.idPath = idPath;
		this.type = type;
		this.index = index;
	}

	/**
	 * @param idKey The key of the JSON value to be used as the ID for the record
	 * @param type The Elasticsearch index type
	 * @param index The Elasticsearch index name
	 */
	public JsonToElasticsearchBulk(String idKey, String type, String index) {
		this(new String[]{idKey}, type, index);
	}

	/**
	 * @param idKey The key of the JSON value to be used as the ID for the record
	 * @param type The Elasticsearch index type
	 * @param index The Elasticsearch index name
	 * @param entitySeparator The separator between entity names in idKey
	 */
	public JsonToElasticsearchBulk(String idKey, String type, String index, String entitySeparator) {
		this(idKey.split(Pattern.quote(entitySeparator)), type, index);
	}

	@Override
	public void process(String obj) {
		StringWriter stringWriter = new StringWriter();
		try {
			Map<String, Object> json = mapper.readValue(obj, MultiMap.class);
			Map<String, Object> detailsMap = new HashMap<String, Object>();
			Map<String, Object> indexMap = new HashMap<String, Object>();
			indexMap.put("index", detailsMap);
			detailsMap.put("_id", findId(json));
			detailsMap.put("_type", type);
			detailsMap.put("_index", index);
			mapper.writeValue(stringWriter, indexMap);
			stringWriter.write("\n");
			mapper.writeValue(stringWriter, json);
		} catch (IOException e) {
			e.printStackTrace();
		}
		getReceiver().process(stringWriter.toString());
	}

	private Object findId(Object value) {
		if (idPath.length < 1) {
			return null;
		}

		for (final String key : idPath) {
			if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				final Map<String, Object> nestedMap = (Map<String, Object>) value;
				value = nestedMap.get(key);
			}
			else {
				return null;
			}
		}

		return value;
	}
}
