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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.culturegraph.mf.exceptions.MetafactureException;
import org.culturegraph.mf.framework.DefaultObjectPipe;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.types.Triple;


/**
 * @author markus geipel
 *
 */
public abstract class AbstractTripleSort extends DefaultObjectPipe<Triple, ObjectReceiver<Triple>> {
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
		}, DECREASING {
			@Override
			public int order(final int indicator) {
				return -indicator;
			}
		};
		public abstract int order(int indicator);
	}


	private static final int KILO = 1024;
	private static final int DEFUALT_BLOCKSIZE = 128 * KILO * KILO;
	private static final int STRING_OVERHEAD = 124;
	
	private final List<Triple> buffer = new ArrayList<Triple>();
	private final List<File> tempFiles = new ArrayList<File>();
	private Compare compare = Compare.SUBJECT;
	private Order order = Order.INCREASING;
	//private Comparator<Triple> comparator = createComparator(compareBy, order);
	private long bufferSizeEstimate;

	private long blockSize = DEFUALT_BLOCKSIZE;

	/**
	 * @param blockSize in MB
	 */
	public final void setBlockSize(final int blockSize) {
		this.blockSize = blockSize * KILO * KILO;
	}

	protected final void setCompare(final Compare compare) {
		this.compare = compare;
	}

	protected final Compare getCompare() {
		return compare;
	}
	
	protected final void setSortOrder(final Order order){
		this.order = order;
	}



	@Override
	public final void process(final Triple namedValue) {

		buffer.add(namedValue);
		// padding is ignored for efficiency (overhead is 45 for name + 45 for
		// value + 8 for namedValue + 28 goodwill)
		bufferSizeEstimate += ((namedValue.getSubject().length() + namedValue.getPredicate().length() + namedValue.getObject().length()) * 2) + STRING_OVERHEAD;
		if (bufferSizeEstimate > blockSize) {
			bufferSizeEstimate = 0;
			try {
				nextBatch();
			} catch (IOException e) {
				throw new MetafactureException("Error writing to temp file after sorting", e);
			}

		}
	}

	private void nextBatch() throws IOException {
		Collections.sort(buffer, createComparator(compare, order));
		final File tempFile = File.createTempFile("sort", "namedValues", null);
		tempFile.deleteOnExit();
		final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tempFile));

		try {
			for (Triple triple : buffer) {
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
			for (Triple triple : buffer) {
				sortedTriple(triple);
			}
			onFinished();
		} else {
			final Comparator<Triple> comparator = createComparator(compare, order);
			final PriorityQueue<SortedTripleFileFacade> queue = new PriorityQueue<SortedTripleFileFacade>(11,
					new Comparator<SortedTripleFileFacade>() {
					//	private final Comparator<Triple> comparator = getComparator();

						@Override
						public int compare(final SortedTripleFileFacade o1, final SortedTripleFileFacade o2) {
							return comparator.compare(o1.peek(), o2.peek());
						}
					});
			try {
				nextBatch();
				for (File file : tempFiles) {
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
			} catch (IOException e) {
				throw new MetafactureException("Error merging temp files", e);
			} finally {
				for (SortedTripleFileFacade sortedFileFacade : queue) {
					sortedFileFacade.close();
				}
			}
		}
	}

	protected void onFinished() {
		// nothing to do
		
	}

	protected abstract void sortedTriple(Triple namedValue);

	public final Comparator<Triple> createComparator(){
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
		for(File file: tempFiles){
			if(file.exists()){
				file.delete();
			}
		}
		tempFiles.clear();
	}
}
