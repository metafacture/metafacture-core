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
package org.culturegraph.mf.stream.pipe;



import org.culturegraph.mf.framework.DefaultObjectReceiver;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests {@link ObjectExceptionCatcher}.
 * 
 * @author Christoph Böhme
 */
public final class ObjectExceptionCatcherTest {

	private static final String OBJECT = "Test Object";
	
	/**
	 * A special exception to make sure the test
	 * is not passed accidentally on a different 
	 * exception.
	 */
	protected static final class ModuleException 
		extends RuntimeException {

		private static final long serialVersionUID = 1L;		
	}
	
	/**
	 * A module whose {@code process()} method always throws 
	 * an exception.
	 * 
	 * @param <T> object type
	 */
	protected static final class FailingModule<T>
		extends DefaultObjectReceiver<T> {
		
		@Override
		public void process(final T obj) {
			throw new ModuleException();
		}
	}
	
	@Test(expected=ModuleException.class)
	public void testSetup() {
		final FailingModule<String> failingModule = new FailingModule<String>();
		
		failingModule.process(OBJECT);
		failingModule.closeStream();
	}
	
	@Test
	public void testCatcher() {
		final ObjectExceptionCatcher<String> catcher = new ObjectExceptionCatcher<String>();
		final FailingModule<String> failingModule = new FailingModule<String>();
		
		catcher.setReceiver(failingModule);
		
		try {
			catcher.process(OBJECT);
			catcher.closeStream();
		} catch (final ModuleException e) {
			Assert.fail(e.toString());
		}
	}

}
