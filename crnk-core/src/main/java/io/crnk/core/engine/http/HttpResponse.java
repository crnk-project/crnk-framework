package io.crnk.core.engine.http;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

	private Map<String, String> headers = new HashMap();

	private int statusCode;

	private byte[] body;


	public void setHeader(String name, String value) {
		headers.put(name, value);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public void setBody(String body) {
		this.body = body.getBytes(Charset.forName("utf8"));
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public void setContentType(String contentType) {
		setHeader(HttpHeaders.HTTP_CONTENT_TYPE, contentType);
	}

	public String getContentType() {
		return getHeader(HttpHeaders.HTTP_CONTENT_TYPE);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[status=" + statusCode + ",bodyLength=" + (body != null ? body.length : 0) + ",headers=" + headers + "]";
	}
}
