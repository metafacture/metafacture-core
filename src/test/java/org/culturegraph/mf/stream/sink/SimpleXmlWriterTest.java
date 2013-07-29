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



import org.culturegraph.mf.framework.DefaultObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link SimpleXmlWriter}.
 *
 * @author Markus Geipel
 *
 */
public final class SimpleXmlWriterTest {


	private static final String TAG = "tag";
	private static final String VALUE = "value";

	//TODO add more tests!


	@Test
	public void testShouldOnlyEscapeFiveChars() {
		final StringBuilder builder = new StringBuilder();

		SimpleXmlWriter.writeEscaped(builder , "&<>'\" üäö");

		Assert.assertEquals("&amp;&lt;&gt;&apos;&quot; üäö", builder.toString());
	}

	@Test
	public void testShouldHandleSeparateRoots(){
		final SimpleXmlWriter writer = new SimpleXmlWriter();
		writer.setRootTag("root");
		writer.setRecordTag("record");
		writer.setWriteXmlHeader(false);

		//separateRoots=false
		final StringBuilder builder1 = new StringBuilder();
		writer.setReceiver(new DefaultObjectReceiver<String>() {
			@Override
			public void process(final String obj) {
				builder1.append(obj);
			}
		});

		writer.setSeparateRoots(false);


		writeTwoRecords(writer);

		Assert.assertEquals("<root><record><tag>value</tag></record><record><tag>value</tag></record></root>", builder1.toString().replaceAll("[\\n\\s]", ""));

		//separateRoots=true
		final StringBuilder builder2 = new StringBuilder();
		writer.setReceiver(new DefaultObjectReceiver<String>() {
			@Override
			public void process(final String obj) {
				builder2.append(obj);
			}
		});

		writer.setSeparateRoots(true);

		writeTwoRecords(writer);

		Assert.assertEquals("<root><record><tag>value</tag></record></root><root><record><tag>value</tag></record></root>", builder2.toString().replaceAll("[\\n\\s]", ""));


	}



	private static void writeTwoRecords(final StreamReceiver writer) {
		writer.startRecord("X");
		writer.literal(TAG, VALUE);
		writer.endRecord();
		writer.startRecord("Y");
		writer.literal(TAG, VALUE);
		writer.endRecord();
		writer.closeStream();
	}


}
