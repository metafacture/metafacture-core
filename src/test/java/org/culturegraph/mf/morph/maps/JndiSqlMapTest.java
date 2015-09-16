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
package org.culturegraph.mf.morph.maps;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;


/**
 * Tests {@link JndiSqlMap}.
 * @author schaeferd
 *
 */
public final class JndiSqlMapTest {
	@Test
	public void testGetDatasource() throws IOException {
		final JndiSqlMap map = new JndiSqlMap();
		map.setDatasource("testDataSource");
		
		assertNotNull(map.getDatasource());
		map.close();
	}
	
}