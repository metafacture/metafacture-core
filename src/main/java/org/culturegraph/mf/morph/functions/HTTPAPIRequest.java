package org.culturegraph.mf.morph.functions;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tgaengler
 */
public class HTTPAPIRequest extends AbstractSimpleStatelessFunction {

	private static final Logger LOG = LoggerFactory.getLogger(HTTPAPIRequest.class);

	private static final String CHUNKED         = "CHUNKED";
	private static final int    CHUNK_SIZE      = 1024;
	private static final int    REQUEST_TIMEOUT = 20000000;

	private static final ClientBuilder BUILDER = ClientBuilder.newBuilder()
			.property(ClientProperties.CHUNKED_ENCODING_SIZE, CHUNK_SIZE)
			.property(ClientProperties.REQUEST_ENTITY_PROCESSING, CHUNKED)
			.property(ClientProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, CHUNK_SIZE)
			.property(ClientProperties.CONNECT_TIMEOUT, REQUEST_TIMEOUT)
			.property(ClientProperties.READ_TIMEOUT, REQUEST_TIMEOUT);

	//	private String uri;

	/**
	 * note: can be modelled as an enum in metamorph.xsd (to simplify input)
	 */
	private String acceptType;

	private String errorString;

	//	public void setUri(final String uri) {
	//
	//		this.uri = uri;
	//	}

	public void setAcceptType(final String acceptType) {

		this.acceptType = acceptType;
	}

	public void setErrorString(final String errorString) {

		this.errorString = errorString;
	}

	/**
	 * note: the input should/must be the URI that should be utilised for the HTTP API request
	 *
	 * @param value
	 * @return
	 */
	@Override
	protected String process(final String value) {

		if (value == null || value.trim().isEmpty()) {

			// no URI available

			LOG.error("no HTTP API request URI available");

			return errorString;
		}

		final URI uri;

		try {

			uri = URI.create(value);
		} catch (final IllegalArgumentException e) {

			// URI is not valid

			LOG.error("HTTP API request URI '{}' is not valid", value);

			return errorString;
		}

		final String uriString = uri.toString();
		final WebTarget target = target(uriString);

		// GET for now
		final Response response = target.request(MediaType.APPLICATION_JSON).get();

		if (response == null) {

			// response is not available

			LOG.error("no response available for HTTP API request GET {}'", uriString);

			return errorString;
		}

		final int status = response.getStatus();

		if (status != 200) {

			// request was not successful

			LOG.error("HTTP API request GET '{}' was not successful - status code = '{}'", uriString, status);

			return errorString;
		}

		final String result = response.readEntity(String.class);

		if (result == null || result.trim().isEmpty()) {

			// result is not available

			LOG.error("result is empty for HTTP API request GET '{}'", uriString);

			return errorString;
		}

		return result;
	}

	private Client client() {

		return BUILDER.build();
	}

	private WebTarget target(final String uri) {

		return client().target(uri);
	}
}
