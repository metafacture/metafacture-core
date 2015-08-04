package org.culturegraph.mf.morph.functions;

import java.io.IOException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author tgaengler
 */
public class HTTPAPIRequestTest {

	@Test
	public void httpAPIRequestTest1() throws IOException {

		final String wikidataQ42 = "https://www.wikidata.org/wiki/Special:EntityData/Q42.json";

		final URL resourceURL = Resources.getResource("morph/q42_result.json");
		final String expectedResult = Resources.toString(resourceURL, Charsets.UTF_8);

		processHTTPAPIRequest(wikidataQ42, MediaType.APPLICATION_JSON, "something unexpected happened", expectedResult);
	}

	private static void processHTTPAPIRequest(final String uri, final String acceptType, final String errorString, final String expectedResult) {

		final HTTPAPIRequest httpapiRequest = new HTTPAPIRequest();
		httpapiRequest.setAcceptType(acceptType);
		httpapiRequest.setErrorString(errorString);

		final String actualResult = httpapiRequest.process(uri);

		Assert.assertNotNull(actualResult);
		Assert.assertEquals(expectedResult, actualResult);
	}

}
