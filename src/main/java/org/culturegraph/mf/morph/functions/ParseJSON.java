package org.culturegraph.mf.morph.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tgaengler
 */
public class ParseJSON extends AbstractCollectionStatelessFunction {

	private static final Logger LOG = LoggerFactory.getLogger(ParseJSON.class);

	private String jsonPathString;
	private String errorString;

	public void setJsonPath(final String jsonPath) {

		jsonPathString = jsonPath;
	}

	public void setErrorString(final String errorString) {

		this.errorString = errorString;
	}

	@Override protected Collection<String> process(final String value) {

		final List<String> processedValues = new ArrayList<>();

		if (jsonPathString == null || jsonPathString.trim().isEmpty()) {

			LOG.error("cannot parse input JSON string - no JSONPath defined.");

			// or shall we return null instead?
			returnErrorString(processedValues);
		}

		if (value == null || value.trim().isEmpty()) {

			LOG.error("input JSON string is not available");

			// or shall we return null instead?
			returnErrorString(processedValues);
		}

		final Object result = JsonPath.read(value, jsonPathString);

		if (result == null) {

			LOG.debug("JSON did not match anything in input JSON string '{}'", value);

			// ro shall we return the error string instead?
			return null;
		}

		if (!Collection.class.isInstance(result)) {

			// single result

			processedValues.add(result.toString());
		} else {

			// multiple results

			final Collection resultCollection = (Collection) result;

			for (final Object resultPart : resultCollection) {

				processedValues.add(resultPart.toString());
			}
		}

		return processedValues;
	}

	private Collection<String> returnErrorString(final List<String> processedValues) {

		processedValues.add(errorString);

		return processedValues;
	}
}
