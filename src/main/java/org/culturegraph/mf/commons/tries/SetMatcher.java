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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

/**
 * Implementation of the Aho-Corasick algorithm.
 *
 * @param <T> type of stored value
 * @author Markus Michael Geipel
 */
public final class SetMatcher<T> {
	private final ACNode<T> root = new ACNode<>(null, 0);
	private boolean isPrepared;

	public void put(final String key, final T value) {
		if (isPrepared) {
			throw new IllegalStateException("keys cannot be added during matching.");
		}

		final int length = key.length();
		ACNode<T> node = root;
		ACNode<T> next;
		for (int i = 0; i < length - 1; ++i) {
			next = node.getNext(key.charAt(i));
			if (next == null) {
				next = node.addNext(key.charAt(i));
			}
			node = next;
		}
		next = node.getNext(key.charAt(length - 1));
		if (next == null) {
			next = node.addNext(key.charAt(length - 1), value);
		} else if (next.getValue() == null) {
			next.setValue(value);
		} else {
			throw new IllegalStateException("Key '" + key + "' already in trie");
		}
	}

	public List<Match<T>> match(final String text) {
		if (!isPrepared) {
			prepare();
			isPrepared = true;
		}
		final List<Match<T>> matches = new ArrayList<Match<T>>();

		ACNode<T> node = root;
		final int length = text.length();
		int index = 0;

		while (index < length) {
			final ACNode<T> next = node.getNext(text.charAt(index));
			if (next != null) {
				node = next;
			} else if (node != root) {
				node = node.getFailure();
				continue;
			}
			++index;
			collectMatches(node, index, matches);
		}
		return matches;
	}

	private void collectMatches(final ACNode<T> node, final int index, final List<Match<T>> matches) {
		//direct hit or hit in chain of failure links?
		ACNode<T> tempNode = node;
		do{
			if (tempNode.getValue() != null) {
				matches.add(new Match<T>(tempNode.getValue(), index - tempNode.getDepth(), tempNode.getDepth()));
			}
			tempNode = tempNode.getFailure();
		}while (tempNode != root);
	}

	private void prepare() {
		final Queue<ACNode<T>> queue = new LinkedList<ACNode<T>>();

		// prepare root
		root.setFailure(root);
		for (ACNode<T> child : root.getNext()) {
			child.setFailure(root);
			queue.add(child);
		}
		// prepare rest
		while (!queue.isEmpty()) {
			final ACNode<T> parent = queue.poll();
			final ACNode<T> parentFailure = parent.getFailure();

			for (Entry<Character, ACNode<T>> link : parent.getLinks()) {
				final char key = link.getKey().charValue();
				final ACNode<T> child = link.getValue();
				ACNode<T> node = parentFailure;

				while (node.getNext(key) == null && node != root) {
					node = node.getFailure();
				}

				if (node.getNext(key) == null) {
					child.setFailure(root);
				} else {
					child.setFailure(node.getNext(key));
				}
				queue.add(child);
			}
		}
	}

	/**
	 * Prints dot description of the automaton to out for visualization in
	 * GraphViz. Used for debugging and education.
	 *
	 * @param out the stream t which the description is written
	 */
	public void printAutomaton(final PrintStream out) {
		out.println("digraph ahocorasick {");
		printDebug(out, root);
		out.println("}");
	}

	private void printDebug(final PrintStream out, final ACNode<T> node) {
		if (node.getValue() == null) {
			out.println(node.hashCode() + " [shape=point label=\"\"]");
		} else {
			out.println(node.hashCode() + " [shape=circle style=filled label=\"\"]");
		}

		if (node.getFailure() != root) {
			out.println(node.hashCode() + " -> " + node.getFailure().hashCode() + "[color=gray]");
		}
		for (Entry<Character, ACNode<T>> link : node.getLinks()) {
			out.println(node.hashCode() + "  -> " + link.getValue().hashCode() + " [label=\"" + link.getKey() + "\"]");
			printDebug(out, link.getValue());
		}
	}

	/**
	 * Describes a match.
	 *
	 * @param <T> type of the stored value
	 */
	public static final class Match<T> {
		private final T value;
		private final int start;
		private final int length;

		public Match(final T value, final int start, final int length) {
			super();
			this.value = value;
			this.start = start;
			this.length = length;
		}

		public T getValue() {
			return value;
		}

		public int getStart() {
			return start;
		}

		public int getLength() {
			return length;
		}

		@Override
		public String toString() {
			return value + " " + start + "+" + length;
		}

	}

}
