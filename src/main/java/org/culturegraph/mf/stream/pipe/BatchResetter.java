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

import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;

/**
 * resets Stream every <code>batchSize</code> records.
 * 
 * @author "Markus Michael Geipel"
 * 
 * 
 */

@Description("Resets flow for every BATCHSIZE records.")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
public final class BatchResetter extends AbstractBatcher {

	@Override
	protected void onBatchComplete() {
		getReceiver().resetStream();
	}
}
