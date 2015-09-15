package org.culturegraph.mf.morph.maps;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests {@link RestMap}.
 * 
 * @author Philipp v. Böselager
 *
 */
public final class RestMapTest {

	private static final String BASE_URL_01 = "http://beta.lobid.org/${key}";
	private static final String TEST_CASE_01 = "organisations/DE-6#!";

	@Test
	public void testGetDatasource() throws IOException {
		final RestMap map = new RestMap();
		map.setUrl(BASE_URL_01);
		String result = map.get(TEST_CASE_01);
		assertTrue(result.contains("51.96286"));
	}

}
