package io.crnk.client.http;

import java.io.IOException;

public interface HttpAdapterResponse {

	boolean isSuccessful();

	String body() throws IOException;

	int code();

	String message();

	String getResponseHeader(String name);

}
