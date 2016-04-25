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
package org.culturegraph.mf.stream.converter;

import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.FluxCommand;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;


/**
 * Adds a String preamle and/or epilogue to the stream
 *
 * @author Markus Geipel
 *
 */
@Description("Adds a String preamle and/or epilogue to the stream")
@In(String.class)
@Out(String.class)
@FluxCommand("add-preamble-epilogue")
public final class PreambleEpilogueAdder extends DefaultObjectPipe<String, ObjectReceiver<String>> {

	private String preamble = "";
	private String epilogue = "";
	private boolean init = true;

	public void setEpilogue(final String epilogue) {
		this.epilogue = epilogue;
	}

	public void setPreamble(final String preamble) {
		this.preamble = preamble;
	}

	@Override
	public void process(final String obj) {
		if(init){
			getReceiver().process(preamble);
			init = false;
		}
		getReceiver().process(obj);
	}

	@Override
	protected void onCloseStream() {
		if(!epilogue.isEmpty()){
			getReceiver().process(epilogue);
		}
	}
}
