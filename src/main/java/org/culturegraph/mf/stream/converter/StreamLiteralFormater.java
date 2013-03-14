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

import org.culturegraph.mf.framework.DefaultStreamPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;


/**
 * 
 * @author Markus Michael Geipel
 * 
 */
@Description("Formats litereals in a stream")
@In(StreamReceiver.class)
@Out(String.class)
public final class StreamLiteralFormater extends DefaultStreamPipe<ObjectReceiver<String>>{
	private static final String DEFAULT_SEPARATOR = "\t";
	private String separator = DEFAULT_SEPARATOR;

	public void setSeparator(final String separator) {
		this.separator = separator;
	}
	

	@Override
	public void literal(final String name, final String value) {
		if(name==null | name.isEmpty()){
			getReceiver().process(value);
		}else{
			getReceiver().process(name+ separator + value);
		}
		
	}
}

