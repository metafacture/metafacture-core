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
 * Tests for {@link WellFormednessChecker}.
 *
 * @author Christoph Böhme
 *
 */
public final class WellFormednessCheckerTest {

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

	private WellFormednessChecker wellFormednessChecker;

	@Before
	public void setup() {
		wellFormednessChecker = new WellFormednessChecker();
	}

	@Test
	public void testShouldAcceptValidStream() {
		wellFormednessChecker.startRecord(RECORD_ID1);
		wellFormednessChecker.literal(LITERAL1, VALUE1);
		wellFormednessChecker.startEntity(ENTITY1);
		wellFormednessChecker.literal(LITERAL2, VALUE2);
		wellFormednessChecker.startEntity(ENTITY2);
		wellFormednessChecker.literal(LITERAL3, VALUE3);
		wellFormednessChecker.endEntity();
		wellFormednessChecker.endEntity();
		wellFormednessChecker.endRecord();
		wellFormednessChecker.startRecord(RECORD_ID2);
		wellFormednessChecker.startEntity(ENTITY3);
		wellFormednessChecker.literal(LITERAL4, VALUE4);
		wellFormednessChecker.endEntity();
		wellFormednessChecker.endRecord();
		wellFormednessChecker.closeStream();
	}

	@Test
	public void testShouldAcceptEmptyStream() {
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnNullRecordId() {
		wellFormednessChecker.startRecord(null);
		wellFormednessChecker.endRecord();
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnNullEntityName() {
		wellFormednessChecker.startRecord(RECORD_ID1);
		wellFormednessChecker.startEntity(null);
		wellFormednessChecker.endEntity();
		wellFormednessChecker.endRecord();
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnNullLiteralName() {
		wellFormednessChecker.startRecord(RECORD_ID1);
		wellFormednessChecker.literal(null, VALUE1);
		wellFormednessChecker.endRecord();
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnStartRecordInsideRecord() {
		wellFormednessChecker.startRecord(RECORD_ID1);
		wellFormednessChecker.startRecord(RECORD_ID2);
		wellFormednessChecker.endRecord();
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnEndRecordOutsideRecord() {
		wellFormednessChecker.endRecord();
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnStartEntityOutsideRecord() {
		wellFormednessChecker.startEntity(ENTITY1);
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnEndEntityOutsideRecord() {
		wellFormednessChecker.endEntity();
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnUnmatchedEndEntity() {
		wellFormednessChecker.startRecord(RECORD_ID1);
		wellFormednessChecker.endEntity();
		wellFormednessChecker.endRecord();
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnLiteralOutsideRecord() {
		wellFormednessChecker.literal(LITERAL1, VALUE1);
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnUnclosedRecord() {
		wellFormednessChecker.startRecord(RECORD_ID1);
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnUnclosedEntityAtEndRecord() {
		wellFormednessChecker.startRecord(RECORD_ID1);
		wellFormednessChecker.startEntity(ENTITY1);
		wellFormednessChecker.endRecord();
		wellFormednessChecker.closeStream();
	}

	@Test(expected=WellformednessException.class)
	public void testShouldFailOnUnclosedEntityAtCloseStream() {
		wellFormednessChecker.startRecord(RECORD_ID1);
		wellFormednessChecker.startEntity(ENTITY1);
		wellFormednessChecker.closeStream();
	}

}
