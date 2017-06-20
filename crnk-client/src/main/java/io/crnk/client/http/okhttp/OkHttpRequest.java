package io.crnk.client.http.okhttp;

import java.io.IOException;

import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpRequest implements HttpAdapterRequest {

	private static final MediaType CONTENT_TYPE = MediaType.parse(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET);

	private Builder builder;

	private OkHttpClient client;

	public OkHttpRequest(OkHttpClient client, String url, HttpMethod method, String requestBody) {
		this.client = client;
		builder = new Request.Builder().url(url);


		RequestBody requestBodyObj = requestBody != null ? RequestBody.create(CONTENT_TYPE, requestBody) : null;
		builder.method(method.toString(), requestBodyObj);
	}

	@Override
	public void header(String name, String value) {
		builder = builder.header(name, value);
	}

	@Override
	public HttpAdapterResponse execute() throws IOException {
		Request request = builder.build();
		Response response = client.newCall(request).execute();
		return new OkHttpResponse(response);
	}

}
