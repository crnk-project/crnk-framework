package io.crnk.client.http.okhttp;

import io.crnk.client.http.HttpAdapterResponse;
import okhttp3.Response;

import java.io.IOException;
import java.util.Set;

public class OkHttpResponse implements HttpAdapterResponse {

	private Response response;

	public OkHttpResponse(Response response) {
		this.response = response;
	}

	@Override
	public boolean isSuccessful() {
		return response.isSuccessful();
	}

	@Override
	public String body() throws IOException {
		return response.body().string();
	}

	@Override
	public int code() {
		return response.code();
	}

	@Override
	public String message() {
		return response.message();
	}

	@Override
	public String getResponseHeader(String name) {
		return response.header(name);
	}

	@Override
	public Set<String> getHeaderNames() {
		return response.headers().names();
	}

}
