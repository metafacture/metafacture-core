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
package org.culturegraph.mf.stream.source;

import java.io.Reader;
import java.util.Collection;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.annotations.ReturnsAvailableArguments;


/**
 * @author Markus Michael Geipel
 * 
 */
@Description("Opens different resources. Type is given in barackets.")
@In(String.class)
@Out(java.io.Reader.class)
public final class MultiOpener implements Opener {

	private static final OpenerFactory OPENER_FACTORY = new OpenerFactory();
	private final Opener opener;

	public MultiOpener(final String type) {
		super();
		if(!OPENER_FACTORY.containsKey(type)){
			throw new IllegalArgumentException("Opener for '" + type + "' does not exist");
		}
		opener = OPENER_FACTORY.newInstance(type);
	}
	
	@ReturnsAvailableArguments
	public static Collection<String> getAvailableArguments(){
		return OPENER_FACTORY.keySet();
	}
	
	@Override
	public <R extends ObjectReceiver<Reader>> R setReceiver(final R receiver) {
		opener.setReceiver(receiver);
		return receiver;
	}

	@Override
	public void process(final String obj) {
		opener.process(obj);
	}

	@Override
	public void resetStream() {
		opener.resetStream();
		
	}

	@Override
	public void closeStream() {
		opener.closeStream();
	}
}
