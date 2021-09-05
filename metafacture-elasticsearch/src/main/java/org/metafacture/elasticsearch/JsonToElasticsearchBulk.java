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

package org.metafacture.elasticsearch;

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
public class JsonToElasticsearchBulk extends DefaultObjectPipe<String, ObjectReceiver<String>> {

    private ObjectMapper mapper = new ObjectMapper();
    private String[] idPath;
    private String type;
    private String index;

    public JsonToElasticsearchBulk() {
        this.idPath = new String[]{};
        this.type = null;
        this.index = null;
    }

    /**
     * As an id is not required it can be omitted.
     *
     * @param type The Elasticsearch index type
     * @param index The Elasticsearch index name
     */
    public JsonToElasticsearchBulk(final String type, final String index) {
        this(new String[] {}, type, index);
    }

    /**
     * @param idPath The key path of the JSON value to be used as the ID for the record
     * @param type The Elasticsearch index type
     * @param index The Elasticsearch index name
     */
    public JsonToElasticsearchBulk(final String[] idPath, final String type, final String index) {
        this.idPath = idPath;
        this.type = type;
        this.index = index;
    }

    /**
     * @param idKey The key of the JSON value to be used as the ID for the record
     * @param type The Elasticsearch index type
     * @param index The Elasticsearch index name
     */
    public JsonToElasticsearchBulk(final String idKey, final String type, final String index) {
        this(new String[]{idKey}, type, index);
    }

    /**
     * @param idKey The key of the JSON value to be used as the ID for the record
     * @param type The Elasticsearch index type
     * @param index The Elasticsearch index name
     * @param entitySeparator The separator between entity names in idKey
     */
    public JsonToElasticsearchBulk(final String idKey, final String type, final String index, final String entitySeparator) {
        this(idKey.split(Pattern.quote(entitySeparator)), type, index);
    }

    public void setIdKey(final String idKey) {
        this.idPath = new String[]{idKey};
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setIndex(final String index) {
        this.index = index;
    }

    @Override
    public void process(final String obj) {
        final StringWriter stringWriter = new StringWriter();
        try {
            final Map<String, Object> json = mapper.readValue(obj, MultiMap.class);
            final Map<String, Object> detailsMap = new HashMap<String, Object>();
            final Map<String, Object> indexMap = new HashMap<String, Object>();
            indexMap.put("index", detailsMap);
            if (idPath.length > 0) {
                detailsMap.put("_id", findId(json));
            }
            detailsMap.put("_type", type);
            detailsMap.put("_index", index);
            mapper.writeValue(stringWriter, indexMap);
            stringWriter.write("\n");
            mapper.writeValue(stringWriter, json);
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        getReceiver().process(stringWriter.toString());
    }

    private Object findId(final Object value) {
        Object newValue = value;

        for (final String key : idPath) {
            if (newValue instanceof Map) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> nestedMap = (Map<String, Object>) newValue;
                newValue = nestedMap.get(key);
            }
            else {
                return null;
            }
        }

        return newValue;
    }

    /**
     * Use a MultiMap with Jackson to collect values from multiple fields with
     * identical names under a single key.
     */
    static class MultiMap extends HashMap<String, Object> { // checkstyle-disable-line IllegalType
        private static final long serialVersionUID = 490682490432334605L;

        MultiMap() {
            // default constructor for Jackson
        }

        @Override
        public Object put(final String key, final Object value) {
            final Object newValue;

            if (containsKey(key)) {
                final Object oldValue = get(key);
                if (oldValue instanceof Set) {
                    @SuppressWarnings("unchecked")
                    final Set<Object> vals = (Set<Object>) oldValue;
                    vals.add(value);
                    newValue = vals;
                }
                else {
                    final Set<Object> set = new HashSet<>(Arrays.asList(oldValue, value));
                    newValue = set.size() == 1 ? value : set;
                }
            }
            else {
                newValue = value;
            }

            return super.put(key, newValue);
        }
    }

}
