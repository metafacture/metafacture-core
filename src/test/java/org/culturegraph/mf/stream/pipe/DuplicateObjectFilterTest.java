/**
 * 
 */
package org.culturegraph.mf.stream.pipe;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.culturegraph.mf.framework.ObjectReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link DuplicateObjectFilter}.
 * 
 * @author Alexander Haffner
 *
 */
public final class DuplicateObjectFilterTest {

	private static final String OBJECT1 = "Object 1";
	private static final String OBJECT2 = "Object 2";

	private DuplicateObjectFilter<String> duplicateObjectFilter;
	
	@Mock
	private ObjectReceiver<String> receiver;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		duplicateObjectFilter = new DuplicateObjectFilter<String>();
		duplicateObjectFilter.setReceiver(receiver);
	}
	
	@After
	public void cleanup() {
		duplicateObjectFilter.closeStream();
	}
	
	@Test
	public void testShouldEliminateDuplicateObjects() {
		duplicateObjectFilter.process(OBJECT1);
		duplicateObjectFilter.process(OBJECT1);
		duplicateObjectFilter.process(OBJECT2);
		
		verify(receiver).process(OBJECT1);
		verify(receiver).process(OBJECT2);
		verifyNoMoreInteractions(receiver);
	}

}
