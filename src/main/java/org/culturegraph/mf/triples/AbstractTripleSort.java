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
package org.culturegraph.mf.triples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.culturegraph.mf.framework.MetafactureException;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.helpers.DefaultObjectPipe;
import org.culturegraph.mf.framework.objects.Triple;

/**
 * @author markus geipel
 *
 */
public abstract class AbstractTripleSort extends DefaultObjectPipe<Triple, ObjectReceiver<Triple>> implements MemoryWarningSystem.Listener {
	/**
	 * specifies the comparator
	 */
	public enum Compare {
		SUBJECT, PREDICATE, OBJECT, ALL;
	}

	/**
	 * sort order
	 *
	 */
	public enum Order {
		INCREASING {
			@Override
			public int order(final int indicator) {
				return indicator;
			}
		},
		DECREASING {
			@Override
			public int order(final int indicator) {
				return -indicator;
			}
		};
		public abstract int order(int indicator);
	}

	private final List<Triple> buffer = new ArrayList<Triple>();
	private final List<File> tempFiles;
	private Compare compare = Compare.SUBJECT;
	private Order order = Order.INCREASING;
	private volatile boolean memoryLow;

	public AbstractTripleSort() {
		MemoryWarningSystem.addListener(this);
		tempFiles = new ArrayList<File>(); // Initialized here to let the
											// compiler enforce the call to
											// super() in subclasses.
	}

	@Override
	public final void memoryLow(final long usedMemory, final long maxMemory) {
		memoryLow = true;
	}

	protected final void setCompare(final Compare compare) {
		this.compare = compare;
	}

	protected final Compare getCompare() {
		return compare;
	}

	protected final void setSortOrder(final Order order) {
		this.order = order;
	}

	@Override
	public final void process(final Triple namedValue) {
		if (memoryLow) {
			try {
				if (!buffer.isEmpty()) {
					nextBatch();
				}
			} catch (final IOException e) {
				throw new MetafactureException("Error writing to temp file after sorting", e);
			} finally {
				memoryLow = false;
			}
		}
		buffer.add(namedValue);
	}

	private void nextBatch() throws IOException {
		Collections.sort(buffer, createComparator(compare, order));
		final File tempFile = File.createTempFile("sort", "namedValues", null);
		tempFile.deleteOnExit();
		final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tempFile));

		try {
			for (final Triple triple : buffer) {
				triple.write(out);
			}
		} finally {
			out.close();
		}
		buffer.clear();
		tempFiles.add(tempFile);
	}

	@Override
	public final void onCloseStream() {

		if (tempFiles.isEmpty()) {
			Collections.sort(buffer, createComparator(compare, order));
			for (final Triple triple : buffer) {
				sortedTriple(triple);
			}
			onFinished();
		} else {
			final Comparator<Triple> comparator = createComparator(compare, order);
			final PriorityQueue<SortedTripleFileFacade> queue = new PriorityQueue<SortedTripleFileFacade>(11,
					new Comparator<SortedTripleFileFacade>() {
						// private final Comparator<Triple> comparator =
						// getComparator();

						@Override
						public int compare(final SortedTripleFileFacade o1, final SortedTripleFileFacade o2) {
							return comparator.compare(o1.peek(), o2.peek());
						}
					});
			try {
				nextBatch();
				for (final File file : tempFiles) {
					queue.add(new SortedTripleFileFacade(file));
				}

				while (queue.size() > 0) {
					final SortedTripleFileFacade sortedFileFacade = queue.poll();
					final Triple triple = sortedFileFacade.pop();
					sortedTriple(triple);
					if (sortedFileFacade.isEmpty()) {
						sortedFileFacade.close();
					} else {
						queue.add(sortedFileFacade);
					}
				}
				onFinished();
			} catch (final IOException e) {
				throw new MetafactureException("Error merging temp files", e);
			} finally {
				for (final SortedTripleFileFacade sortedFileFacade : queue) {
					sortedFileFacade.close();
				}
			}
		}
		MemoryWarningSystem.removeListener(this);
	}

	protected void onFinished() {
		// nothing to do

	}

	protected abstract void sortedTriple(Triple namedValue);

	public final Comparator<Triple> createComparator() {
		return createComparator(compare, order);
	}

	public static Comparator<Triple> createComparator(final Compare compareBy, final Order order) {
		final Comparator<Triple> comparator;
		switch (compareBy) {
		case ALL:
			comparator = new Comparator<Triple>() {
				@Override
				public int compare(final Triple o1, final Triple o2) {
					return order.order(o1.compareTo(o2));
				}
			};
			break;
		case OBJECT:
			comparator = new Comparator<Triple>() {
				@Override
				public int compare(final Triple o1, final Triple o2) {
					return order.order(o1.getObject().compareTo(o2.getObject()));
				}
			};
			break;
		case SUBJECT:
			comparator = new Comparator<Triple>() {
				@Override
				public int compare(final Triple o1, final Triple o2) {
					return order.order(o1.getSubject().compareTo(o2.getSubject()));
				}
			};
			break;
		case PREDICATE:
		default:
			comparator = new Comparator<Triple>() {
				@Override
				public int compare(final Triple o1, final Triple o2) {
					return order.order(o1.getPredicate().compareTo(o2.getPredicate()));
				}
			};
			break;
		}

		return comparator;
	}

	@Override
	public final void onResetStream() {
		buffer.clear();
		for (final File file : tempFiles) {
			if (file.exists()) {
				file.delete();
			}
		}
		tempFiles.clear();
	}
}
