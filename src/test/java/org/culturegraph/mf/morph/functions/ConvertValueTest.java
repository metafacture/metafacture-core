package org.culturegraph.mf.morph.functions;

import java.util.Map;

import org.culturegraph.mf.morph.functions.model.ValueConverter;
import org.culturegraph.mf.morph.functions.model.ValueType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author tgaengler
 */
public class ConvertValueTest {

	public static final String DEFAULT_TEST_LITERAL_VALUE = "test literal value";
	public static final String DEFAULT_TEST_RESOURCE_VALUE = "http://example.com";
	public static final String DEFAULT_TEST_BNODE_VALUE = "bnode-1234";
	public static final String DEFAULT_ERROR_STRING       = "something went wrong";

	@Test
	public void literalTest() {

		final String value = DEFAULT_TEST_LITERAL_VALUE;
		final ValueType type = ValueType.Literal;
		final String errorString = DEFAULT_ERROR_STRING;
		final String expectedValue = value;
		final ValueType literal = ValueType.Literal;

		processConvertValue(value, type, errorString, expectedValue, literal);
	}

	@Test
	public void defaultValueTest() {

		final String value = DEFAULT_TEST_LITERAL_VALUE;
		final ValueType type = null;
		final String errorString = DEFAULT_ERROR_STRING;
		final String expectedValue = value;
		final ValueType expectedType = ValueType.Literal;

		processConvertValue(value, type, errorString, expectedValue, expectedType);
	}

	@Test
	public void resourceTest() {

		final String value = DEFAULT_TEST_RESOURCE_VALUE;
		final ValueType type = ValueType.Resource;
		final String errorString = DEFAULT_ERROR_STRING;
		final String expectedValue = value;
		final ValueType expectedType = ValueType.Resource;

		processConvertValue(value, type, errorString, expectedValue, expectedType);
	}

	@Test
	public void literalURITest() {

		final String value = DEFAULT_TEST_RESOURCE_VALUE;
		final ValueType type = ValueType.Literal;
		final String errorString = DEFAULT_ERROR_STRING;
		final String expectedValue = value;
		final ValueType literal = ValueType.Literal;

		processConvertValue(value, type, errorString, expectedValue, literal);
	}

	@Test
	public void nonValidResourceTest() {

		final String value = "öklav";
		final ValueType type = ValueType.Resource;
		final String errorString = DEFAULT_ERROR_STRING;
		final String expectedValue = errorString;
		final ValueType expectedType = ValueType.Error;

		processConvertValue(value, type, errorString, expectedValue, expectedType);
	}

	@Test
	public void bnodeTest() {

		final String value = DEFAULT_TEST_BNODE_VALUE;
		final ValueType type = ValueType.BNode;
		final String errorString = DEFAULT_ERROR_STRING;
		final String expectedValue = value;
		final ValueType expectedType = ValueType.BNode;

		processConvertValue(value, type, errorString, expectedValue, expectedType);
	}

	private static void processConvertValue(final String value, final ValueType type, final String errorString, final String expectedResult, final ValueType expectedType) {

		final ConvertValue convertValue = new ConvertValue();

		if (type != null) {

			convertValue.setType(type.toString());
		}

		convertValue.setErrorString(errorString);

		final String actualResult = convertValue.process(value);

		Assert.assertNotNull(actualResult);

		final Map.Entry<ValueType, String> decodedValueEntry = ValueConverter.decodeTypeInfo(actualResult);
		final ValueType actualType = decodedValueEntry.getKey();
		final String actualValue = decodedValueEntry.getValue();

		Assert.assertEquals(expectedType, actualType);
		Assert.assertEquals(expectedResult, actualValue);
	}
}
