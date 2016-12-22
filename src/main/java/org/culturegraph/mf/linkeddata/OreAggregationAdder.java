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
package org.culturegraph.mf.linkeddata;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.culturegraph.mf.commons.ResourceUtil;
import org.culturegraph.mf.commons.types.ListMap;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;



/**
 * Adds ore:Aggregation to an Europeana Data Model stream. The aggregation id is
 * set by emitting literal('aggregation_id', id).
 *
 * @author Markus Michael Geipel
 *
 */
@Description("adds ore:Aggregation to an Europeana Data Model stream. The aggregation id is set by emitting literal('aggregation_id', id)")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("add-oreaggregation")
public final class OreAggregationAdder extends DefaultStreamPipe<StreamReceiver> {

	private static final String RDF_ABOUT = "~rdf:about";
	private static final ListMap<String, String> AGGREGATED_ENTITIES = new ListMap<String, String>();
	private static final String ORE_AGGREGATION_PROPERTIES = "ore-aggregation.properties";
	private static final String ORE_AGGREGATION = "ore:Aggregation";
	private static final String AGGREGATION_ID = "aggregation_id";
	private static final String RDF_REFERENCE = "~rdf:resource";
	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*,\\s*");
	private final Deque<String> entityStack = new LinkedList<String>();
	private final ListMap<String, String> aggregation = new ListMap<String, String>();
	private String aggregationId;

	static {
		final Properties properties;
		try {
			properties = ResourceUtil.loadProperties(ORE_AGGREGATION_PROPERTIES);
		} catch (IOException e) {
			throw new MetafactureException("Failed to load properties", e);
		}
		for (Entry<Object, Object> entry : properties.entrySet()) {
			final String[] parts = SPLIT_PATTERN.split(entry.getValue().toString());
			final String name = entry.getKey().toString();
			for (String value : parts) {
				AGGREGATED_ENTITIES.add(name, value);
			}
		}
	}

	@Override
	public void startRecord(final String identifier) {
		entityStack.clear();
		aggregationId = identifier;
		getReceiver().startRecord(identifier);
	}

	@Override
	public void endRecord() {
		writeAggregation();
		aggregation.clear();
		getReceiver().endRecord();
	}

	private void writeAggregation() {
		if (!aggregation.isEmpty()) {
			final StreamReceiver receiver = getReceiver();
			receiver.startEntity(ORE_AGGREGATION);
			receiver.literal(RDF_ABOUT, aggregationId);
			for (Entry<String, List<String>> entry : aggregation.entrySet()) {
				final String key = entry.getKey();
				if (AGGREGATED_ENTITIES.containsKey(key)) {

					for (String entity : AGGREGATED_ENTITIES.get(key)) {
						for (String value : entry.getValue()) {
							receiver.startEntity(entity);
							receiver.literal(RDF_REFERENCE, value);
							receiver.endEntity();
						}
					}
				} else {
					for (String value : entry.getValue()) {
						receiver.literal(key, value);
					}
				}
			}
			receiver.endEntity();
		}
	}

	@Override
	public void startEntity(final String name) {
		entityStack.push(name);
		getReceiver().startEntity(name);
	}

	@Override
	public void endEntity() {
		entityStack.pop();
		getReceiver().endEntity();
	}

	@Override
	public void literal(final String name, final String value) {
		if (entityStack.isEmpty()) {
			if (AGGREGATION_ID.equals(name)) {
				aggregationId = value;
			} else {
				aggregation.add(name, value);
			}
			return;
		}

		if (entityStack.size()==1 && RDF_ABOUT.equals(name) && AGGREGATED_ENTITIES.containsKey(entityStack.peek())) {
			aggregation.add(entityStack.peek(), value);
		}
		getReceiver().literal(name, value);
	}
}
