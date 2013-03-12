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

import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.types.NamedValue;
import org.culturegraph.mf.types.Triple;

/**
 * @author markus geipel
 *
 */
@Description("Counts triples")
@In(NamedValue.class)
@Out(NamedValue.class)
public final class TripleCount extends AbstractTripleSort {

	public static final String DEFAULT_COUNTP_REDICATE = "count";
	private Triple current;
	private int count;
	private String countPredicate = DEFAULT_COUNTP_REDICATE;
	
	@Override
	protected void sortedTriple(final Triple triple) {
		
		if(current==null){
			current = triple;
		}

		if(getComparator().compare(current, triple)==0){
			++count;
		}else{
			writeResult();
			current = triple;
			count = 1;
		}
	}
	
	public void setCountPredicate(final String countPredicate) {
		this.countPredicate = countPredicate;
	}
	
	@Override
	protected void onFinished() {
		writeResult();
	}
	
	private void writeResult() {
		final CompareBy compareBy = getComparatorType();
		switch (compareBy) {
		case ALL:
			getReceiver().process(new Triple(current.toString(), countPredicate , String.valueOf(count)));
			break;
		case OBJECT:
			getReceiver().process(new Triple(current.getObject(), countPredicate, String.valueOf(count)));
			break;
		case PREDICATE:
			getReceiver().process(new Triple(current.getPredicate(), countPredicate, String.valueOf(count)));
			break;
		case SUBJECT:
		default:
			getReceiver().process(new Triple(current.getSubject(), countPredicate, String.valueOf(count)));
			break;
		}
	}

	public void setCountBy(final CompareBy countBy){
		setComparator(countBy);
	}

}
