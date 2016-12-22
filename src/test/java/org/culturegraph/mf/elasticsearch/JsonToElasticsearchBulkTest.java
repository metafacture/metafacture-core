/*
 * Copyright 2016 hbz
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
package org.culturegraph.mf.elasticsearch;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link JsonToElasticsearchBulk}.
 *
 * @author Jens Wille
 *
 */
public final class JsonToElasticsearchBulkTest {

	private static final String LITERAL1 = "L1";
	private static final String LITERAL4 = "L4";

	private static final String VALUE1 = "V1";

	private static final String ENTITY1 = "En1";
	private static final String ENTITY2 = "En2";
	private static final String ENTITY3 = "En3";

	private static final String TYPE1  = "T1";
	private static final String INDEX1 = "I1";

	private static final String METADATA = "{'index':{'_index':'I1','_type':'T1','_id':%s}}";

	private static final String ENTITY_SEPARATOR1 = ".";
	private static final String ENTITY_SEPARATOR2 = ":";

	private JsonToElasticsearchBulk bulk;

	@Mock
	private ObjectReceiver<String> receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void cleanup() {
		bulk.closeStream();
	}

	@Test
	public void testShouldPreserveLists() {
		setBulk(LITERAL1);
		shouldExtractId("{'L1':'V1','Li1':['V1','V2','V3']}");
	}

	@Test
	public void testShouldPreserveNestedLists() {
		setBulk(LITERAL1);
		shouldExtractId("{'L1':'V1','Li1':[['V1','V2'],['V3','V4']]}");
	}

	@Test
	public void testShouldPreserveEntitiesInLists() {
		setBulk(LITERAL1);
		shouldExtractId("{'L1':'V1','Li1':[{'L1':'V1','L2':'V2'},{'L3':'V3','L4':'V4'}]}");
	}

	@Test
	public void testShouldCollectDuplicateNamesInList() {
		setBulk(LITERAL1);
		shouldExtractId("{'L1':'V1','L1':'V2'}", "['V1','V2']", "{'L1':['V1','V2']}");
	}

	@Test
	public void testShouldExtractIdKey() {
		setBulk(LITERAL1);
		shouldExtractId("{'L1':'V1','L2':'V2','L3':'V3'}");
	}

	@Test
	public void testShouldNotExtractMissingIdKey() {
		setBulk(LITERAL4);
		shouldNotExtractId("{'L1':'V1','L2':'V2','L3':'V3'}");
	}

	@Test
	public void testShouldExtractIdKeyWithSeparator() {
		setBulk(LITERAL1 + ENTITY_SEPARATOR1 + LITERAL4);
		shouldExtractId("{'L1.L4':'V1','L2':'V2','L3':'V3'}");
	}

	@Test
	public void testShouldNotExtractMissingIdKeyWithSeparator() {
		setBulk(LITERAL1 + ENTITY_SEPARATOR1 + LITERAL4);
		shouldNotExtractId("{'L1':'V1','L2':'V2','L3':'V3'}");
	}

	@Test
	public void testShouldNotExtractEmptyIdKey() {
		setBulk("");
		shouldNotExtractId("{'L1':'V1','L2':'V2','L3':'V3'}");
	}

	@Test
	public void testShouldNotExtractEmptyIdPath() {
		setBulk(new String[0]);
		shouldNotExtractId("{'L1':'V1','L2':'V2','L3':'V3'}");
	}

	@Test
	public void testShouldExtractEntityAsId() {
		setBulk(ENTITY1);
		shouldExtractId("{'En1':{'L1':'V1'}}", "{'L1':'V1'}");
	}

	@Test
	public void testShouldExtractIdPath() {
		setBulk(new String[]{ENTITY1, LITERAL1});
		shouldExtractId("{'En2':{'L1':'V1','L2':'V2'},'En1':{'L1':'V1','L2':'V2'}}");
	}

	@Test
	public void testShouldExtractJoinedIdPath() {
		setBulk(new String[]{ENTITY1, LITERAL1}, ENTITY_SEPARATOR1);
		shouldExtractId("{'En2':{'L1':'V1','L2':'V2'},'En1':{'L1':'V1','L2':'V2'}}");
	}

	@Test
	public void testShouldNotExtractJoinedIdPathWithDifferentSeparator() {
		setBulk(new String[]{ENTITY1, LITERAL1}, ENTITY_SEPARATOR2);
		shouldNotExtractId("{'En2':{'L1':'V1','L2':'V2'},'En1':{'L1':'V1','L2':'V2'}}");
	}

	@Test
	public void testShouldNotExtractMissingIdPath() {
		setBulk(new String[]{ENTITY1, LITERAL4});
		shouldNotExtractId("{'En2':{'L1':'V1','L2':'V2'},'En1':{'L1':'V1','L2':'V2'}}");
	}

	@Test
	public void testShouldNotExtractMissingEntityIdPath() {
		setBulk(new String[]{ENTITY3, LITERAL1});
		shouldNotExtractId("{'En2':{'L1':'V1','L2':'V2'},'En1':{'L1':'V1','L2':'V2'}}");
	}

	@Test
	public void testShouldExtractNestedIdPath() {
		setBulk(new String[]{ENTITY1, ENTITY2, LITERAL1});
		shouldExtractId("{'En1':{'En2':{'L1':'V1'}}}");
	}

	@Test
	public void testShouldExtractJoinedNestedIdPath() {
		setBulk(new String[]{ENTITY1, ENTITY2, LITERAL1}, ENTITY_SEPARATOR1);
		shouldExtractId("{'En1':{'En2':{'L1':'V1'}}}");
	}

	@Test
	public void testShouldNotExtractJoinedNestedIdPathWithDifferentSeparator() {
		setBulk(new String[]{ENTITY1, ENTITY2, LITERAL1}, ENTITY_SEPARATOR2);
		shouldNotExtractId("{'En1':{'En2':{'L1':'V1'}}}");
	}

	@Test
	public void testShouldNotExtractMissingNestedIdPath() {
		setBulk(new String[]{ENTITY1, ENTITY2, LITERAL4});
		shouldNotExtractId("{'En1':{'En2':{'L1':'V1'}}}");
	}

	@Test
	public void testShouldNotExtractMissingNestedEntityIdPath() {
		setBulk(new String[]{ENTITY1, ENTITY3, LITERAL1});
		shouldNotExtractId("{'En1':{'En2':{'L1':'V1'}}}");
	}

	@Test
	public void testShouldNotExtractIntermediateEntityIdPath() {
		setBulk(new String[]{ENTITY1, ENTITY1, LITERAL4});
		shouldNotExtractId("{'En1':{'L1':'V1'}}");
	}

	/*
	 * Utility methods to set bulk indexer based on given ID key/path.
	 */
	private void setBulk(final String idKey) {
		bulk = new JsonToElasticsearchBulk(idKey, TYPE1, INDEX1);
	}

	private void setBulk(final String[] idPath) {
		bulk = new JsonToElasticsearchBulk(idPath, TYPE1, INDEX1);
	}

	private void setBulk(final String[] idPath, final String entitySeparator) {
		final String idKey = String.join(ENTITY_SEPARATOR1, idPath);
		bulk = new JsonToElasticsearchBulk(idKey, TYPE1, INDEX1, entitySeparator);
	}

	/*
	 * Utility methods to test bulk indexer ID extraction.
	 */
	private void shouldExtractId(final String obj, final String idValue, final String resultObj) {
		bulk.setReceiver(receiver);
		bulk.process(fixQuotes(obj));

		verify(receiver).process(fixQuotes(String.format(METADATA, idValue) + "\n" + resultObj));
		verifyNoMoreInteractions(receiver);
	}

	private void shouldExtractId(final String obj, final String idValue) {
		shouldExtractId(obj, idValue, obj);
	}

	private void shouldExtractId(final String obj) {
		shouldExtractId(obj, "'" + VALUE1 + "'");
	}

	private void shouldNotExtractId(final String obj) {
		shouldExtractId(obj, "null");
	}

	/*
	 * Utility method which replaces all single quotes in a string with double quotes.
	 * This allows to specify the JSON output in the test cases without having to wrap
	 * each bit of text in escaped double quotes.
	 */
	private String fixQuotes(final String str) {
		return str.replace('\'', '"');
	}

}
