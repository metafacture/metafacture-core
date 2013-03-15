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
package org.culturegraph.mf.stream.converter;

import static org.junit.Assert.assertEquals;

import org.culturegraph.mf.framework.DefaultObjectReceiver;
import org.junit.Test;

/**
 * Test for {@link JsonEncoder}.
 * 
 * @author Christoph Böhme
 *
 */
public final class JsonEncoderTest {

	private static final String SIMPLE_JSON = "{\"lit1\":\"val1\",\"ent1\":{\"lit2\":\"val2\",\"lit3\":\"val3\"}}";
	private static final String LIST_JSON = "{\"list\":[\"1\",\"2\",\"3\"],\"lit\":\"val\"}";

	@Test
	public void testSimpleJson() {
		final JsonEncoder encoder = new JsonEncoder();
		encoder.setReceiver(new DefaultObjectReceiver<String>() {
			@Override
			public void process(final String str) {
				assertEquals(SIMPLE_JSON, str);
			}
		});
		
		encoder.startRecord("");
		encoder.literal("lit1", "val1");
		encoder.startEntity("ent1");
		encoder.literal("lit2", "val2");
		encoder.literal("lit3", "val3");
		encoder.endEntity();
		encoder.endRecord();
		encoder.closeStream();
	}
	
	@Test
	public void testListJson() {
		final JsonEncoder encoder = new JsonEncoder();
		encoder.setReceiver(new DefaultObjectReceiver<String>() {
			@Override
			public void process(final String str) {
				assertEquals(LIST_JSON, str);
			}
		});
		
		encoder.startRecord("");
		encoder.startEntity("list[]");
		encoder.literal("", "1");
		encoder.literal("", "2");
		encoder.literal("", "3");
		encoder.endEntity();
		encoder.literal("lit", "val");
		encoder.endRecord();
		encoder.closeStream();
	}

}
