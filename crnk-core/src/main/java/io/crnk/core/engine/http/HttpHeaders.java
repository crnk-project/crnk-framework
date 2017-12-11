package io.crnk.core.engine.http;

public class HttpHeaders {

	private HttpHeaders() {
	}

	public static final String HTTP_HEADER_ACCEPT = "Accept";

	public static final String HTTP_HEADER_CRNK_COMPACT = "Crnk-Compact";

	public static final String HTTP_CONTENT_TYPE = "Content-Type";

	public static final String JSONAPI_CONTENT_TYPE = "application/vnd.api+json";

	public static final String DEFAULT_CHARSET = "utf-8";

	public static final String JSONAPI_CONTENT_TYPE_AND_CHARSET = JSONAPI_CONTENT_TYPE + "; charset=" +
			DEFAULT_CHARSET;

}
