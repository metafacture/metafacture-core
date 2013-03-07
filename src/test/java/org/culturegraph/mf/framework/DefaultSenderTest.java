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
package org.culturegraph.mf.framework;



import org.junit.Assert;
import org.junit.Test;


/**
 * Tests Contract of {@link DefaultSender}.
 * @author Markus M Geipel
 *
 */
public final class DefaultSenderTest {

	/**
	 *  onCloseStream() must be called only once even if closeSteam is called several times
	 */
	@Test
	public void testMultipleCloseStreamInvocations() {
		final CloseCounter closeCounter = new CloseCounter();
		
		Assert.assertEquals(0, closeCounter.getCount());
		Assert.assertFalse(closeCounter.isClosed());
		
		closeCounter.closeStream();
		Assert.assertEquals(1, closeCounter.getCount());
		Assert.assertTrue(closeCounter.isClosed());
		
		closeCounter.closeStream();
		Assert.assertEquals(1, closeCounter.getCount());
		Assert.assertTrue(closeCounter.isClosed());
		
		closeCounter.closeStream();
		Assert.assertEquals(1, closeCounter.getCount());
		Assert.assertTrue(closeCounter.isClosed());
		
	}
	
	/**
	 *	counts invocation of onCloseStream()
	 */
	protected static final class CloseCounter extends DefaultSender<ObjectReceiver<Object>>{
		private int count;
		
		@Override
		protected void onCloseStream() {
			++count;
		}
		
		public int getCount() {
			return count;
		}
	}

}
