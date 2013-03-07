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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.ReturnsAvailableArguments;


/**
 * Writes objects to stdout or a file
 * @param <T> object type
 * 
 * @author Christoph Böhme
 *
 */

@Description("Writes objects to stdout or a file")
@In(Object.class)
public final class ObjectWriter<T> implements ObjectReceiver<T> {

	private static final String STDOUT = "stdout";
	private static final List<String> ARGUMENTS = Collections.unmodifiableList(Arrays.asList(STDOUT, "PATH"));
	
	private final ObjectReceiver<T> objectWriter;
	
	public ObjectWriter(final String destination){
		if(STDOUT.equals(destination)){
			objectWriter = new ObjectStdoutWriter<T>();
		}else{
			objectWriter = new ObjectFileWriter<T>(destination);
		}
	}
	
	@ReturnsAvailableArguments
	public static Collection<String> getArguments(){
		return ARGUMENTS;
	}

	@Override
	public void process(final T obj) {
		objectWriter.process(obj);
	}

	@Override
	public void resetStream() {
		objectWriter.resetStream();
	}
	
	@Override
	public void closeStream() {
		objectWriter.closeStream();
	}

}
