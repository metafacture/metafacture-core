/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.culturegraph.mf.commons.tries;

/**
 * A simple Trie, nothing fancy at all.
 *
 * @param <P> type of value stored
 * @author Markus Michael Geipel
 */
public final class SimpleTrie<P> {
	private final Node<P> root = new Node<>(null);

	public void put(final String key, final P value){

		Node<P> node = root;
		Node<P> next;
		final int length = key.length();
		for (int i = 0; i < length-1; ++i) {
			next = node.getNext(key.charAt(i));
			if(next==null){
				next = node.addNext(key.charAt(i));
			}
			node = next;
		}
		next = node.getNext(key.charAt(length-1));
		if(next==null){
			next = node.addNext(key.charAt(length-1), value);
		}else{
			throw new IllegalStateException("Value '" + value + "' already in trie");
		}
	}

	public P get(final String key){
		Node<P> node = root;
		final int length = key.length();
		for (int i = 0; i < length; ++i) {
			node = node.getNext(key.charAt(i));
			if(node==null){
				return null;
			}
		}
		return node.getValue();
	}

	/**
	 * Node in the trie.
	 *
	 * @param <P> type of the value associated with this node.
	 */
	private static final class Node<P> {
		private final P value;
		private final CharMap<Node<P>> links = new CharMap<Node<P>>();

		public Node(final P value) {
			this.value = value;
		}

		public Node<P> addNext(final char key){
			return addNext(key, null);
		}

		public Node<P> addNext(final char key, final P value){
			final Node<P> next = new Node<P>(value);
			links.put(key, next);
			return next;
		}

		public P getValue(){
			return value;
		}

		public Node<P> getNext(final char key){
			return links.get(key);
		}
	}
}
