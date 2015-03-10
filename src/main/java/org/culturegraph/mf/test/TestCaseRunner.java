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
package org.culturegraph.mf.test;

import java.io.InputStream;
import java.util.List;

import org.culturegraph.mf.exceptions.FormatException;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;


/**
 * @author Christoph Böhme <c.boehme@dnb.de>, Markus Michael Geipel
 *
 */
final class TestCaseRunner extends ParentRunner<TestCase> {

	private final Class<?> clazz;
	private final List<TestCase> testCases;
	private final String testDefinition;


	public TestCaseRunner(final Class<?> clazz,  final String testDefinition)
			throws InitializationError {
		super(clazz);
		this.clazz = clazz;
		final InputStream inputStream = clazz.getResourceAsStream(testDefinition);
		if(null==inputStream){
			throw new IllegalArgumentException("'" + testDefinition + "' does not exist!");
		}
		this.testCases = TestCaseLoader.load(inputStream);
		this.testDefinition = testDefinition;
	}

	@Override
	protected String getName() {
		return testDefinition;
	}

	@Override
	protected List<TestCase> getChildren() {
		return testCases;
	}

	@Override
	protected Description describeChild(final TestCase child) {
		return Description.createTestDescription(clazz, child.getName());
	}


	@Override
	protected void runChild(final TestCase child, final RunNotifier notifier) {
		if (child.isIgnore()) {
			notifier.fireTestIgnored(describeChild(child));
		} else {
			notifier.fireTestStarted(describeChild(child));
			try {
				child.run();
			} catch (final FormatException e) {
				notifier.fireTestFailure(new Failure(describeChild(child),
						new AssertionError(e)));
			} catch (final Throwable e) {
				notifier.fireTestFailure(new Failure(describeChild(child), e));
			} finally {
				notifier.fireTestFinished(describeChild(child));
			}
		}
	}
}