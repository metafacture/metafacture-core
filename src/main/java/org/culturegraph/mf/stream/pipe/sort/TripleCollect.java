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
package org.culturegraph.mf.stream.pipe.sort;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.Triple;

/**
 * Collects named values to form records.
 * 
 * @author markus geipel
 * 
 */
@Description("Collects named values to form records. The name becomes the id, the value is split by 'separator' into name and value")
@In(Triple.class)
@Out(StreamReceiver.class)
public final class TripleCollect extends DefaultObjectPipe<Triple, StreamReceiver> {

	private String currentSubject;

	@Override
	public void process(final Triple triple) {
		if (currentSubject == null) {
			currentSubject = triple.getSubject();
			getReceiver().startRecord(currentSubject);
		}

		if (currentSubject.equals(triple.getSubject())) {
			getReceiver().literal(triple.getPredicate(), triple.getObject());
		} else {
			getReceiver().endRecord();
			currentSubject = triple.getSubject();
			getReceiver().startRecord(currentSubject);
			getReceiver().literal(triple.getPredicate(), triple.getObject());
		}
	}

	@Override
	protected void onResetStream() {
		currentSubject = null;
		getReceiver().endRecord();
	}

	@Override
	protected void onCloseStream() {
		currentSubject = null;
		getReceiver().endRecord();
	}

}
