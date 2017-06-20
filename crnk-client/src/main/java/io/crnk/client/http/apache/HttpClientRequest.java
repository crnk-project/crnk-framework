package io.crnk.client.http.apache;

import java.io.IOException;

import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

public class HttpClientRequest implements HttpAdapterRequest {

	private static final ContentType CONTENT_TYPE = ContentType.create(HttpHeaders.JSONAPI_CONTENT_TYPE,
			HttpHeaders.DEFAULT_CHARSET);

	private HttpRequestBase requestBase;

	private CloseableHttpClient impl;

	public HttpClientRequest(CloseableHttpClient impl, String url, HttpMethod method, String requestBody) {
		this.impl = impl;
		if (method == HttpMethod.GET) {
			requestBase = new HttpGet(url);
		} else if (method == HttpMethod.POST) {
			HttpPost post = new HttpPost(url);
			post.setEntity(new StringEntity(requestBody, CONTENT_TYPE));
			requestBase = post;
		} else if (method == HttpMethod.PATCH) {
			HttpPatch post = new HttpPatch(url);
			post.setEntity(new StringEntity(requestBody, CONTENT_TYPE));
			requestBase = post;
		} else if (method == HttpMethod.DELETE) {
			requestBase = new HttpDelete(url);
		} else {
			throw new UnsupportedOperationException(method.toString());
		}

	}

	@Override
	public void header(String name, String value) {
		requestBase.setHeader(name, value);
	}

	@Override
	public HttpAdapterResponse execute() throws IOException {
		return new HttpClientResponse(impl.execute(requestBase));
	}
}
