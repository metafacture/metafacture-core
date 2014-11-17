/*
 *  Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.morph;

import java.util.HashMap;
import java.util.Map;

import org.culturegraph.mf.framework.DefaultStreamReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.types.MultiMap;
import org.culturegraph.mf.types.NamedValue;
import org.culturegraph.mf.util.xml.Location;
import org.junit.Assert;
import org.junit.Test;


/**
 * tests {@link Metamorph}
 *
 * @author Markus Michael Geipel
 */
public final class MetamorphBasicTest implements NamedValueReceiver {

	private static final String NAME = "name";
	private static final String VALUE = "s234234ldkfj";
	private static final String ENTITY_NAME = "dfsdf";
	private static final String LITERAL_NAME = "fghgh";
	private static final String MATCHING_PATH = ENTITY_NAME + '.' + LITERAL_NAME;
	private static final String NON_MATCHING_PATH1 = "s234234";
	private static final String NON_MATCHING_PATH2 = ENTITY_NAME + ".lskdj";
	private static final StreamReceiver EMPTY_RECEIVER = new DefaultStreamReceiver() {
		@Override
		public void literal(final String name, final String value) {
			// nothing
		}
	};
	private static final String FEEDBACK_VAR = "@var";
	private static final String MAP_NAME = "sdfklsjef";

	private NamedValue namedValue;

	private static Metamorph newMetamorphWithData(final NamedValueReceiver receiver){
		final Metamorph metamorph = new Metamorph();
		metamorph.setReceiver(EMPTY_RECEIVER);
		final Data data = new Data();
		data.setName(NAME);
		receiver.addNamedValueSource(data);
		metamorph.registerNamedValueReceiver(MATCHING_PATH, data);
		return metamorph;
	}

	@Test
	public void testSimpleMapping() {
		final Metamorph metamorph = newMetamorphWithData(this);
		namedValue = null;
		metamorph.startRecord(null);

		//simple mapping without entity
		metamorph.literal(NON_MATCHING_PATH1, VALUE);
		Assert.assertNull(namedValue);

		metamorph.literal(MATCHING_PATH, VALUE);
		Assert.assertNotNull(namedValue);
		Assert.assertEquals(VALUE, namedValue.getValue());
		namedValue = null;

		// mapping with entity
		metamorph.startEntity(ENTITY_NAME);
		metamorph.literal(LITERAL_NAME, VALUE);
		Assert.assertFalse(namedValue==null);
		Assert.assertEquals(VALUE, namedValue.getValue());
		namedValue = null;

		metamorph.literal(NON_MATCHING_PATH2, VALUE);
		Assert.assertNull(namedValue);

		metamorph.endEntity();
		metamorph.literal(LITERAL_NAME, VALUE);
		Assert.assertNull(namedValue);
	}

	@Test
	public void testMultiMap(){
		final Metamorph metamorph = new Metamorph();
		final Map<String, String> map = new HashMap<String, String>();
		map.put(NAME, VALUE);

		metamorph.putMap(MAP_NAME, map);
		Assert.assertNotNull(metamorph.getMap(MAP_NAME));
		Assert.assertNotNull(metamorph.getValue(MAP_NAME,NAME));
		Assert.assertEquals(VALUE, metamorph.getValue(MAP_NAME,NAME));

		map.put(MultiMap.DEFAULT_MAP_KEY, VALUE);
		Assert.assertNotNull(metamorph.getValue(MAP_NAME,"sdfadsfsdf"));
		Assert.assertEquals(VALUE, metamorph.getValue(MAP_NAME,"sdfsdf"));

	}

	@Test
	public void testFeedback() {

		final Metamorph metamorph = new Metamorph();
		metamorph.setReceiver(EMPTY_RECEIVER);
		Data data;

		data = new Data();
		data.setName(FEEDBACK_VAR);
		metamorph.addNamedValueSource(data);
		metamorph.registerNamedValueReceiver(MATCHING_PATH, data);

		data = new Data();
		data.setName(NAME);
		addNamedValueSource(data);
		metamorph.registerNamedValueReceiver(FEEDBACK_VAR, data);

		namedValue = null;

		metamorph.startRecord(null);
		metamorph.literal(MATCHING_PATH, VALUE);
		Assert.assertFalse(namedValue==null);
		Assert.assertEquals(VALUE, namedValue.getValue());
		namedValue = null;


	}


	@Test(expected=IllegalStateException.class)
	public void testEntityBorderBalanceCheck1(){
		final Metamorph metamorph = new Metamorph();
		metamorph.setReceiver(EMPTY_RECEIVER);

		metamorph.startRecord(null);
		metamorph.startEntity(ENTITY_NAME);
		metamorph.startEntity(ENTITY_NAME);
		metamorph.endEntity();
		metamorph.endRecord();
	}

	@Test(expected=IllegalStateException.class)
	public void testEntityBorderBalanceCheck2(){
		final Metamorph metamorph = new Metamorph();
		metamorph.setReceiver(EMPTY_RECEIVER);

		metamorph.startRecord(null);
		metamorph.startEntity(ENTITY_NAME);
		metamorph.endEntity();
		metamorph.endEntity();
		metamorph.endRecord();
	}




	@Override
	public void receive(final String name, final String value, final NamedValueSource source, final int recordCount, final int entityCount) {
		this.namedValue = new NamedValue(name, value);

	}

	@Override
	public void addNamedValueSource(final NamedValueSource namedValueSource) {
		namedValueSource.setNamedValueReceiver(this);
	}

	@Override
	public void setSourceLocation(final Location sourceLocation) {
		// Nothing to do
	}

	@Override
	public Location getSourceLocation() {
		// Nothing to do
		return null;
	}

}
