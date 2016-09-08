/*
 * Copyright 2016 Christoph Böhme
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
package org.culturegraph.mf.test;

import java.net.URL;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

/**
 * Executes test cases defined in a metamorph-test file.
 *
 * @author Christoph Böhme
 * @author Markus Michael Geipel
 *
 */
final class MetamorphTestRunner extends ParentRunner<MetamorphTestCase> {

	private final List<MetamorphTestCase> metamorphTestCases;
	private final String testDefinition;

	MetamorphTestRunner(final Class<?> testClass, final String testDefinition)
			throws InitializationError {
		super(testClass);
		final URL testDefinitionUrl = testClass.getResource(testDefinition);
		if (testDefinitionUrl == null) {
			throw new InitializationError("'" + testDefinition + "' does not exist!");
		}
		this.metamorphTestCases = MetamorphTestLoader.load(testDefinitionUrl);
		this.testDefinition = testDefinition;
	}

	@Override
	protected String getName() {
		final int nameLength = testDefinition.indexOf('.');
		if (nameLength < 0) {
			return "xml: " + testDefinition;
		}
		return "xml: " + testDefinition.substring(0, nameLength);
	}

	@Override
	protected List<MetamorphTestCase> getChildren() {
		return metamorphTestCases;
	}

	@Override
	protected Description describeChild(final MetamorphTestCase child) {
		return Description.createTestDescription(getName(), child.getName());
	}

	@Override
	protected void runChild(final MetamorphTestCase child,
			final RunNotifier notifier) {
		final Description description = describeChild(child);
		if (child.isIgnore()) {
			notifier.fireTestIgnored(description);
		} else {
			runLeaf(child, description, notifier);
		}
	}

}
