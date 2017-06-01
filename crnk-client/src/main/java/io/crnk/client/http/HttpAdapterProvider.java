package io.crnk.client.http;

public interface HttpAdapterProvider {

	boolean isAvailable();

	HttpAdapter newInstance();

}
