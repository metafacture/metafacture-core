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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

/**
 * Binds a list of Metamorph-Test resources to a class.
 *
 * @author Christoph Böhme
 * @author Markus Geipel
 *
 */
public final class MetamorphTestSuite extends ParentRunner<Runner> {

	private final List<Runner> runners;

	public MetamorphTestSuite(final Class<?> suiteRoot)
			throws InitializationError {
		super(suiteRoot);
		runners = loadDefinitions(suiteRoot);
	}

	private static List<Runner> loadDefinitions(final Class<?> suiteRoot)
			throws InitializationError{
		final List<Runner> runners = new ArrayList<>();
		for (final String testDef : getTestDefinitionNames(suiteRoot)) {
			runners.add(new MetamorphTestRunner(suiteRoot, testDef));
		}
		return runners;
	}

	private static String[] getTestDefinitionNames(final Class<?> suiteRoot){
		final TestDefinitions testDefs =
				suiteRoot.getAnnotation(TestDefinitions.class);
		if (testDefs == null) {
			// if no xmls are given assume an xml with the same name as the class:
			return new String[]{suiteRoot.getSimpleName() + ".xml"};
		}
		return testDefs.value();
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@Override
	protected Description describeChild(final Runner child) {
		return child.getDescription();
	}

	@Override
	protected void runChild(final Runner child, final RunNotifier notifier) {
		child.run(notifier);
	}

	/**
	 * Defines the test definition resources to run when the annoteated class is
	 * tested.
	 *
	 * @author Christoph Böhme
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface TestDefinitions {
		/**
		 * @return the files containing the test case definitions
		 */
		String[] value();
	}

}
