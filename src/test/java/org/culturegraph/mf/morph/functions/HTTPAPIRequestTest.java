package org.culturegraph.mf.morph.functions;

import java.io.IOException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author tgaengler
 */
public class HTTPAPIRequestTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void httpAPIRequestTest1() throws IOException {

		final String jsonTestMD5 = "http://md5.jsontest.com/?text=example_text";

		final URL resourceURL = Resources.getResource("morph/jsontestmd5_result.json");
		final String expectedResult = Resources.toString(resourceURL, Charsets.UTF_8);

		processHTTPAPIRequest(jsonTestMD5, MediaType.APPLICATION_JSON, "something unexpected happened", expectedResult);
	}

	private static void processHTTPAPIRequest(final String uri, final String acceptType, final String errorString, final String expectedResult)
			throws IOException {

		final HTTPAPIRequest httpapiRequest = new HTTPAPIRequest();
		httpapiRequest.setAcceptType(acceptType);
		httpapiRequest.setErrorString(errorString);

		final String actualResult = httpapiRequest.process(uri);

		Assert.assertNotNull(actualResult);

		final JsonNode expectedResultNode = MAPPER.readValue(expectedResult, JsonNode.class);
		final JsonNode actualResultNode = MAPPER.readValue(actualResult, JsonNode.class);

		final String finalExpectedResult = MAPPER.writeValueAsString(expectedResultNode);
		final String finalActualResult = MAPPER.writeValueAsString(actualResultNode);

		Assert.assertEquals(finalExpectedResult, finalActualResult);
	}

}
