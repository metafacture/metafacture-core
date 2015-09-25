package org.culturegraph.mf.morph.functions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.culturegraph.mf.morph.functions.model.ValueConverter;
import org.culturegraph.mf.morph.functions.model.ValueType;

/**
 * @author tgaengler
 */
public class ConvertValue extends AbstractSimpleStatelessFunction {

	private ValueType type;
	private String    errorString;

	public void setType(final String typeString) {

		type = ValueType.getByName(typeString);
	}

	public void setErrorString(final String errorString) {

		this.errorString = errorString;
	}

	@Override protected String process(final String value) {

		if (type == null) {

			type = ValueType.Literal;
		}

		if (ValueType.Resource.equals(type)) {

			try {

				final URI uri = new URI(value);
				uri.toURL();
			} catch (final URISyntaxException | MalformedURLException | IllegalArgumentException e) {

				// value is not a URI, hence cannot be utilised as URI for creating/matching a resource

				return ValueConverter.encodeTypeInfo(errorString, ValueType.Error);
			}
		}

		return ValueConverter.encodeTypeInfo(value, type);
	}
}
