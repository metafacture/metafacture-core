/*
 * Copyright 2016 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.mangling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link EntityPathTracker}.
 *
 * @author Christoph Böhme
 *
 */
public class EntityPathTrackerTest {

	private EntityPathTracker pathTracker;

	@Before
	public void initSystemUnderTest() {
		pathTracker = new EntityPathTracker();
	}

	@Test
	public void getCurrentPath_shouldReturnEmptyPathIfProcessingHasNotStarted() {
		assertTrue(pathTracker.getCurrentPath().isEmpty());
	}

	@Test
	public void getCurrentPath_shouldReturnEmptyPathIfNotInRecord() {
		pathTracker.startRecord("1");
		pathTracker.endRecord();
		assertTrue(pathTracker.getCurrentPath().isEmpty());
	}

	@Test
	public void getCurrentPath_shouldReturnPathToCurrentEntity() {
		pathTracker.startRecord("1");
		assertEquals("", pathTracker.getCurrentPath());
		pathTracker.startEntity("granny");
		assertEquals("granny", pathTracker.getCurrentPath());
		pathTracker.startEntity("mommy");
		assertEquals("granny.mommy", pathTracker.getCurrentPath());
		pathTracker.startEntity("me");
		assertEquals("granny.mommy.me", pathTracker.getCurrentPath());
		pathTracker.endEntity();
		assertEquals("granny.mommy", pathTracker.getCurrentPath());
		pathTracker.startEntity("my-sister");
		assertEquals("granny.mommy.my-sister", pathTracker.getCurrentPath());
		pathTracker.endEntity();
		assertEquals("granny.mommy", pathTracker.getCurrentPath());
		pathTracker.endEntity();
		assertEquals("granny", pathTracker.getCurrentPath());
		pathTracker.endEntity();
		assertEquals("", pathTracker.getCurrentPath());
	}

	@Test
	public void startRecord_shouldResetPath() {
		pathTracker.startRecord("1");
		pathTracker.startEntity("entity");
		assertEquals("entity", pathTracker.getCurrentPath());

		pathTracker.startRecord("2");
		assertTrue(pathTracker.getCurrentPath().isEmpty());
	}

	@Test
	public void resetStream_shouldResetPath() {
		pathTracker.startRecord("1");
		pathTracker.startEntity("entity");
		assertEquals("entity", pathTracker.getCurrentPath());

		pathTracker.resetStream();
		assertTrue(pathTracker.getCurrentPath().isEmpty());
	}

	@Test
	public void closeStream_shouldResetPath() {
		pathTracker.startRecord("1");
		pathTracker.startEntity("entity");
		assertEquals("entity", pathTracker.getCurrentPath());

		pathTracker.closeStream();
		assertTrue(pathTracker.getCurrentPath().isEmpty());
	}

	@Test
	public void getCurrentPathWith_shouldAppendLiteralNameToPath() {
		pathTracker.startRecord("1");
		pathTracker.startEntity("entity");

		assertEquals("entity.literal", pathTracker.getCurrentPathWith("literal"));
	}

	@Test
	public void getCurrentPathWith_shouldReturnOnlyLiteralNameIfNotInEntity() {
		pathTracker.startRecord("1");

		assertEquals("literal", pathTracker.getCurrentPathWith("literal"));
	}

	@Test
	public void getCurrentEntityName_shouldReturnNullIfProcessingNotStarted() {
		assertNull(pathTracker.getCurrentEntityName());
	}

	@Test
	public void getCurrentEntityName_shouldReturnNullIfNotInRecord() {
		pathTracker.startRecord("1");
		pathTracker.endRecord();

		assertNull(pathTracker.getCurrentEntityName());
	}

	@Test
	public void getCurrentEntityName_shouldReturnNameOfCurrentEntity() {
		pathTracker.startRecord("1");
		assertNull(pathTracker.getCurrentEntityName());
		pathTracker.startEntity("grandad");
		assertEquals("grandad", pathTracker.getCurrentEntityName());
		pathTracker.startEntity("daddy");
		assertEquals("daddy", pathTracker.getCurrentEntityName());
		pathTracker.endEntity();
		assertEquals("grandad", pathTracker.getCurrentEntityName());
		pathTracker.endEntity();
		assertNull(pathTracker.getCurrentEntityName());
		pathTracker.endRecord();
	}

}
