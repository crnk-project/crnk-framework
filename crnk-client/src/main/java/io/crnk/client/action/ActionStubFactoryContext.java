package io.crnk.client.action;

import io.crnk.client.http.HttpAdapter;
import io.crnk.core.engine.url.ServiceUrlProvider;

public interface ActionStubFactoryContext {

	ServiceUrlProvider getServiceUrlProvider();

	HttpAdapter getHttpAdapter();

}
