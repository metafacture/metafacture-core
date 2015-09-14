package org.culturegraph.mf.morph.maps;

import static org.junit.Assert.assertEquals;

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

	private static final String BASE_URL_01 = "http://quaoar1.hbz-nrw.de:7070/${key}";
	private static final String BASE_URL_02 = "http://quaoar1.hbz-nrw.de:7070/${key}/${key}/${key}/${key}/${key}";
	private static final String TEST_CASE_01 = "long/J%C3%BClicher%20Stra%C3%9Fe/6/K%C3%B6ln/Germany";
	private static final List<String> TEST_CASE_02 = Arrays.asList("long", "J%C3%BClicher%20Stra%C3%9Fe", "6",
			"K%C3%B6ln", "Germany");

	@Test
	public void testGetDatasource() throws IOException {
		final RestMap map = new RestMap();
		map.setUrl(BASE_URL_01);
		String result = map.get(TEST_CASE_01);
		assertEquals("6.93551400842729", result);
	}

	@Test
	public void testGetDatasourceWithKeyList() throws IOException {
		final RestMap map = new RestMap();
		map.setUrl(BASE_URL_02);
		String result = map.get(TEST_CASE_02);
		assertEquals("6.93551400842729", result);
	}

}
