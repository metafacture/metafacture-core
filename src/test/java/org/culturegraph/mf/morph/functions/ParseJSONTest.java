package org.culturegraph.mf.morph.functions;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author tgaengler
 */
public class ParseJSONTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	public void parseJSONTest1() throws IOException {

		final URL resourceURL = Resources.getResource("morph/q42_result.json");
		final String inputString = Resources.toString(resourceURL, Charsets.UTF_8);

		final String jsonPath = "$.entities.Q42.title";
		final String errorString = "something unexpected happened";
		final String expectedResult = "\"Q42\"";

		processParseJSON(inputString, jsonPath, errorString, expectedResult);
	}

	@Test
	public void parseJSONTest2() throws IOException {

		final URL resourceURL = Resources.getResource("morph/q42_result.json");
		final String inputString = Resources.toString(resourceURL, Charsets.UTF_8);

		// note: we need to apply the second wildcard, to iterate over the JSON array for the aliases of a language
		final String jsonPath = "$.entities.Q42.aliases.*.*.value";
		final String errorString = "something unexpected happened";
		final String expectedResult = "[\"Douglas Noël Adams\",\"Douglas Noel Adams\",\"Адамс, Дуглас\",\"Douglas Noël Adams\",\"Douglas Noel Adams\",\"Douglas Noël Adams\",\"Douglas Noël Adams\",\"Douglas Noel Adams\",\"Дуглас Адамс\",\"亞當斯\",\"Douglas Noel Adams\",\"Douglas Noel Adams\",\"Douglas Noël Adams\",\"Douglas Noel Adams\",\"Ադամս, Դուգլաս\",\"Ντάγκλας Νόελ Άνταμς\"]";

		processParseJSON(inputString, jsonPath, errorString, expectedResult);
	}

	private static void processParseJSON(final String inputString, final String jsonPath, final String errorString, final String expectedResult)
			throws IOException {

		final ParseJSON parseJSON = new ParseJSON();
		parseJSON.setJsonPath(jsonPath);
		parseJSON.setErrorString(errorString);

		final Collection<String> actualResultCollection = parseJSON.process(inputString);

		Assert.assertNotNull(actualResultCollection);
		Assert.assertFalse(actualResultCollection.isEmpty());

		final JsonNode resultNode;

		if (actualResultCollection.size() > 1) {

			resultNode = MAPPER.createArrayNode();

			for (final String actualResultPart : actualResultCollection) {

				((ArrayNode) resultNode).add(actualResultPart);
			}
		} else {

			resultNode = new TextNode(actualResultCollection.iterator().next());
		}

		final String actualResult = MAPPER.writeValueAsString(resultNode);

		Assert.assertEquals(expectedResult, actualResult);

	}
}
