package org.culturegraph.mf.morph.maps;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Tests {@link RestMap}.
 * 
 * @author Philipp v. Böselager
 *
 */
public final class RestMapTest {

	private static final String BASE_URL_01 = "http://beta.lobid.org/${key}";
	private static final String BASE_URL_02 = "http://beta.lobid.org/${key}/${key}";
	private static final String TEST_CASE_01 = "organisations/DE-6#!";
	private static final List<String> TEST_CASE_02 = Arrays.asList("organisations", "DE-6#!");

	@Test
	public void testGetDatasource() throws IOException {
		final RestMap map = new RestMap();
		map.setUrl(BASE_URL_01);
		String result = map.get(TEST_CASE_01);
		assertTrue(result.contains("51.96286"));
	}

	@Test
	public void testGetDatasourceWithKeyList() throws IOException {
		final RestMap map = new RestMap();
		map.setUrl(BASE_URL_02);
		String result = map.get(TEST_CASE_02);
		assertTrue(result.contains("51.96286"));
	}

}
