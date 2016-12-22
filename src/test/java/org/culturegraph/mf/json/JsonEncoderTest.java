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
package org.culturegraph.mf.json;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link JsonEncoder}.
 *
 * @author Christoph Böhme
 *
 */
public final class JsonEncoderTest {

	private static final String LITERAL1 = "L1";
	private static final String LITERAL2 = "L2";
	private static final String LITERAL3 = "L3";
	private static final String LITERAL4 = "L4";

	private static final String VALUE1 = "V1";
	private static final String VALUE2 = "V2";
	private static final String VALUE3 = "V3";
	private static final String VALUE4 = "V4";

	private static final String ENTITY1 = "En1";
	private static final String ENTITY2 = "En2";

	private static final String LIST1 = "Li1[]";
	private static final String LIST2 = "Li2[]";
	private static final String LIST3 = "Li3[]";

	private JsonEncoder encoder;

	@Mock
	private ObjectReceiver<String> receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		encoder = new JsonEncoder();
		encoder.setReceiver(receiver);
	}

	@After
	public void cleanup() {
		encoder.closeStream();
	}

	@Test
	public void testShouldEncodeLiterals() {
		encoder.startRecord("");
		encoder.literal(LITERAL1, VALUE1);
		encoder.literal(LITERAL2, VALUE2);
		encoder.literal(LITERAL3, VALUE3);
		encoder.endRecord();

		verify(receiver).process(fixQuotes("{'L1':'V1','L2':'V2','L3':'V3'}"));
	}

	@Test
	public void testShouldEncodeEntities() {
		encoder.startRecord("");
		encoder.startEntity(ENTITY1);
		encoder.literal(LITERAL1, VALUE1);
		encoder.literal(LITERAL2, VALUE2);
		encoder.endEntity();
		encoder.startEntity(ENTITY2);
		encoder.literal(LITERAL1, VALUE1);
		encoder.literal(LITERAL2, VALUE2);
		encoder.endEntity();
		encoder.endRecord();

		verify(receiver).process(fixQuotes("{'En1':{'L1':'V1','L2':'V2'},'En2':{'L1':'V1','L2':'V2'}}"));
	}

	@Test
	public void testShouldEncodeNestedEntities() {
		encoder.startRecord("");
		encoder.startEntity(ENTITY1);
		encoder.startEntity(ENTITY2);
		encoder.literal(LITERAL1, VALUE1);
		encoder.endEntity();
		encoder.endEntity();
		encoder.endRecord();

		verify(receiver).process(fixQuotes("{'En1':{'En2':{'L1':'V1'}}}"));
	}

	@Test
	public void testShouldEncodeMarkedEntitiesAsList() {
		encoder.startRecord("");
		encoder.startEntity(LIST1);
		encoder.literal(LITERAL1, VALUE1);
		encoder.literal(LITERAL2, VALUE2);
		encoder.literal(LITERAL3, VALUE3);
		encoder.endEntity();
		encoder.endRecord();

		verify(receiver).process(fixQuotes("{'Li1':['V1','V2','V3']}"));
	}

	@Test
	public void testShouldEncodeEntitiesInLists() {
		encoder.startRecord("");
		encoder.startEntity(LIST1);
		encoder.startEntity(ENTITY1);
		encoder.literal(LITERAL1, VALUE1);
		encoder.literal(LITERAL2, VALUE2);
		encoder.endEntity();
		encoder.startEntity(ENTITY2);
		encoder.literal(LITERAL3, VALUE3);
		encoder.literal(LITERAL4, VALUE4);
		encoder.endEntity();
		encoder.endEntity();
		encoder.endRecord();

		verify(receiver).process(fixQuotes("{'Li1':[{'L1':'V1','L2':'V2'},{'L3':'V3','L4':'V4'}]}"));
	}

	@Test
	public void testShouldEncodeNestedLists() {
		encoder.startRecord("");
		encoder.startEntity(LIST1);
		encoder.startEntity(LIST2);
		encoder.literal(LITERAL1, VALUE1);
		encoder.literal(LITERAL2, VALUE2);
		encoder.endEntity();
		encoder.startEntity(LIST3);
		encoder.literal(LITERAL3, VALUE3);
		encoder.literal(LITERAL4, VALUE4);
		encoder.endEntity();
		encoder.endEntity();
		encoder.endRecord();

		verify(receiver).process(fixQuotes("{'Li1':[['V1','V2'],['V3','V4']]}"));
	}

	@Test
	public void testShouldOutputDuplicateNames() {
		encoder.startRecord("");
		encoder.literal(LITERAL1, VALUE1);
		encoder.literal(LITERAL1, VALUE2);
		encoder.endRecord();

		verify(receiver).process(fixQuotes("{'L1':'V1','L1':'V2'}"));
	}

	@Test
	public void testIssue152ShouldNotPrefixOutputWithSpaces() {
		encoder.startRecord("");
		encoder.literal(LITERAL1, VALUE1);
		encoder.endRecord();
		encoder.startRecord("");
		encoder.literal(LITERAL2, VALUE2);
		encoder.endRecord();

		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).process(fixQuotes("{'L1':'V1'}"));
		ordered.verify(receiver).process(fixQuotes("{'L2':'V2'}"));
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
