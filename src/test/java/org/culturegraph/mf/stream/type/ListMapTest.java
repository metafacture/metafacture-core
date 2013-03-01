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
package org.culturegraph.mf.stream.type;

import junit.framework.Assert;

import org.culturegraph.mf.types.ListMap;
import org.junit.Test;


/**
 * tests {@link ListMap}
 * 
 * @author Markus Michael Geipel
 * 
 */
public final class ListMapTest {
	private static final String VALUE1 = "v1";
	private static final String NAME1 = "n1";
	private static final String VALUE2 = "v2";
	private static final String NAME2 = "n2";

	@Test
	public void test() {
		final ListMap<String, String> listMap = new ListMap<String, String>();
		
		Assert.assertNull(listMap.getFirst(NAME1));
		listMap.put(NAME1, VALUE1);
		Assert.assertNotNull(listMap.getFirst(NAME1));
		Assert.assertEquals(VALUE1, listMap.getFirst(NAME1));
		
		listMap.put(NAME1, VALUE2);
		Assert.assertNotNull(listMap.getFirst(NAME1));
		Assert.assertEquals(VALUE1, listMap.getFirst(NAME1));
		
		Assert.assertNotNull(listMap.get(NAME1));
		Assert.assertEquals(2, listMap.get(NAME1).size());
		Assert.assertTrue(listMap.get(NAME1).contains(VALUE2));
		
		Assert.assertNotNull(listMap.get(NAME2));
		Assert.assertEquals(0, listMap.get(NAME2).size());
		
		listMap.put(NAME2, VALUE2);
		Assert.assertNotNull(listMap.getFirst(NAME2));
		listMap.clearKey(NAME2);
		Assert.assertNull(listMap.getFirst(NAME2));
		Assert.assertNotNull(listMap.getFirst(NAME1));
		
		listMap.clearAllKeys();
		Assert.assertNull(listMap.getFirst(NAME1));
		
	}
}
