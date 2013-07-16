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
package org.culturegraph.mf.stream.sink;

import org.culturegraph.mf.exceptions.WellformednessException;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link WellformednessChecker}.
 *
 * @author Christoph Böhme
 *
 */
public final class WellformednessCheckerTest {

	private static final String RECORD_ID1 = "id1";
	private static final String RECORD_ID2 = "id2";
	private static final String ENTITY1 = "entity1";
	private static final String ENTITY2 = "entity2";
	private static final String ENTITY3 = "entity3";
	private static final String LITERAL1 = "literal1";
	private static final String LITERAL2 = "literal2";
	private static final String LITERAL3 = "literal3";
	private static final String LITERAL4 = "literal4";
	private static final String VALUE1 = "value1";
	private static final String VALUE2 = "value2";
	private static final String VALUE3 = "value3";
	private static final String VALUE4 = "value4";

	private WellformednessChecker wellformednessChecker;

	@Before
	public void setup() {
		wellformednessChecker = new WellformednessChecker();
	}

	@Test
	public void testShouldAcceptValidStream() {
		wellformednessChecker.startRecord(RECORD_ID1);
		wellformednessChecker.literal(LITERAL1, VALUE1);
		wellformednessChecker.startEntity(ENTITY1);
		wellformednessChecker.literal(LITERAL2, VALUE2);
		wellformednessChecker.startEntity(ENTITY2);
		wellformednessChecker.literal(LITERAL3, VALUE3);
		wellformednessChecker.endEntity();
		wellformednessChecker.endEntity();
		wellformednessChecker.endRecord();
		wellformednessChecker.startRecord(RECORD_ID2);
		wellformednessChecker.startEntity(ENTITY3);
		wellformednessChecker.literal(LITERAL4, VALUE4);
		wellformednessChecker.endEntity();
		wellformednessChecker.endRecord();
		wellformednessChecker.closeStream();
	}

	@Test
	public void testShouldAcceptEmptyStream() {
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnNullRecordId() {
		wellformednessChecker.startRecord(null);
		wellformednessChecker.endRecord();
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnNullEntityName() {
		wellformednessChecker.startRecord(RECORD_ID1);
		wellformednessChecker.startEntity(null);
		wellformednessChecker.endEntity();
		wellformednessChecker.endRecord();
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnNullLiteralName() {
		wellformednessChecker.startRecord(RECORD_ID1);
		wellformednessChecker.literal(null, VALUE1);
		wellformednessChecker.endRecord();
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnStartRecordInsideRecord() {
		wellformednessChecker.startRecord(RECORD_ID1);
		wellformednessChecker.startRecord(RECORD_ID2);
		wellformednessChecker.endRecord();
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnEndRecordOutsideRecord() {
		wellformednessChecker.endRecord();
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnStartEntityOutsideRecord() {
		wellformednessChecker.startEntity(ENTITY1);
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnEndEntityOutsideRecord() {
		wellformednessChecker.endEntity();
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnUnmatchedEndEntity() {
		wellformednessChecker.startRecord(RECORD_ID1);
		wellformednessChecker.endEntity();
		wellformednessChecker.endRecord();
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnLiteralOutsideRecord() {
		wellformednessChecker.literal(LITERAL1, VALUE1);
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnUnclosedRecord() {
		wellformednessChecker.startRecord(RECORD_ID1);
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnUnclosedEntityAtEndRecord() {
		wellformednessChecker.startRecord(RECORD_ID1);
		wellformednessChecker.startEntity(ENTITY1);
		wellformednessChecker.endRecord();
		wellformednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnUnclosedEntityAtCloseStream() {
		wellformednessChecker.startRecord(RECORD_ID1);
		wellformednessChecker.startEntity(ENTITY1);
		wellformednessChecker.closeStream();
	}

}
