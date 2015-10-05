/*
 *  Copyright 2013, 2014 hbz
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
package org.culturegraph.mf.stream.converter.xml;

import static org.mockito.Mockito.inOrder;

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.stream.DataFilePath;
import org.culturegraph.mf.stream.source.ResourceOpener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Pascal Christoph (dr0i)
 */
public final class AlephMabXmlHandlerTest {

	private ResourceOpener opener;
	private XmlDecoder xmlDecoder;
	private AlephMabXmlHandler mabXmlHandler;

	@Mock
	private StreamReceiver receiver;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		opener = new ResourceOpener();
		xmlDecoder = new XmlDecoder();
		mabXmlHandler = new AlephMabXmlHandler();
		opener.setReceiver(xmlDecoder).setReceiver(mabXmlHandler).setReceiver(receiver);
	}

	@After
	public void cleanup() {
		opener.closeStream();
	}

	@Test
	public void testShouldIgnoreCharDataNotInARecord() {

		opener.process(DataFilePath.ALEPH_MAB_XML);
		final InOrder ordered = inOrder(receiver);
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).startEntity("001-1");
		ordered.verify(receiver).literal("a", "HT010726584");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).startEntity("331-1");
		ordered.verify(receiver).literal("a", "Physics of plasmas");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).startEntity("902-1");
		ordered.verify(receiver).literal("s", "Zeitschrift");
		ordered.verify(receiver).literal("9", "(DE-588)4067488-5");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
		ordered.verify(receiver).startRecord("");
		ordered.verify(receiver).startEntity("001-1");
		ordered.verify(receiver).literal("a", "HT018700720");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).startEntity("100b1");
		ordered.verify(receiver).literal("p", "Amrhein, Ludwig");
		ordered.verify(receiver).endEntity();
		ordered.verify(receiver).endRecord();
	}

}
