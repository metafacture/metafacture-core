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
package org.culturegraph.mf.util.tries;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;


/**
 * @author Markus Michael Geipel
 *
 * @param <P>
 */
public final class ACNode<P> {
	private P value;
	private final CharMap<ACNode<P>> links = new CharMap<ACNode<P>>();
	private ACNode<P> failure;
	private final int depth;
	
	public ACNode(final P value, final int depth) {
		this.value = value;
		this.depth = depth;
	}
	
	public ACNode<P> addNext(final char key){
		return addNext(key, null);
	}
	
	public ACNode<P> addNext(final char key, final P value){
		final ACNode<P> next = new ACNode<P>(value, depth+1);
		links.put(key, next);
		return next;
	}
	
	public void setValue(final P value) {
		this.value = value;
	}
	
	public P getValue(){
		return value;
	}
	
	public ACNode<P> getNext(final char key){
		return links.get(key);
	}

	public ACNode<P> getFailure() {
		return failure;
	}

	public void setFailure(final ACNode<P> failure) {
		this.failure = failure;
	}
	
	public int getDepth() {
		return depth;
	}

	public Collection<ACNode<P>> getNext(){
		return links.values();
	}

	public Set<Entry<Character, ACNode<P>>> getLinks() {
		return links.entrySet();
	}
	
}
