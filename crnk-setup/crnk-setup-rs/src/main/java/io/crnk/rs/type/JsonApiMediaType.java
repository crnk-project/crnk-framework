package io.crnk.rs.type;

import jakarta.ws.rs.core.MediaType;

public final class JsonApiMediaType {


	private JsonApiMediaType() {
		// private since only a util
	}

	/**
	 * A {@code String} constant representing {@value #APPLICATION_JSON_API} media type.
	 */
	public final static String APPLICATION_JSON_API = "application/vnd.api+json";

	/**
	 * A {@link MediaType} constant representing {@value #APPLICATION_JSON_API} media type.
	 */
	public final static MediaType APPLICATION_JSON_API_TYPE = new MediaType("application", "vnd.api+json");
}

