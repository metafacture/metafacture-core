/*
 * Copyright 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;

/**
 * Tests for class (@link MetamorphTestRunner}.
 *
 * @author Christoph Böhme
 */
public final class TestCaseRunnerMetamorphTest {

	@Test
	public void issue213ShouldNotInitAnnotationsArrayWithNull() throws InitializationError {

		final MetamorphTestRunner runner = new MetamorphTestRunner(Dummy.class, "/org/culturegraph/mf/test/test-case-runner-test-dummy.xml");
		final MetamorphTestCase metamorphTestCase = runner.getChildren().get(0);
		final Description description = runner.describeChild(metamorphTestCase);

		assertNotNull(description.getAnnotations());
	}

	public static final class Dummy {};

}
