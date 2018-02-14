package io.crnk.client;

import java.util.concurrent.TimeUnit;

import io.crnk.client.action.JerseyActionStubFactory;
import io.crnk.client.module.ClientTestModule;

/**
 * Class creates a CrnkClient to serialize links as JSON objects.<br />
 * All tests can be found in {@link AbstractProxiedObjectsClientTest}.
 */
public class ObjectLinkProxiedObjectsClientTest extends AbstractProxiedObjectsClientTest {

	@Override
	protected void createClient() {
		client = new CrnkClient(getBaseUri().toString(), CrnkClient.ClientType.OBJECT_LINKS);
		client.addModule(new ClientTestModule());
		// tag::jerseyStubFactory[]
		client.setActionStubFactory(JerseyActionStubFactory.newInstance());
		// end::jerseyStubFactory[]
		client.getHttpAdapter().setReceiveTimeout(10000000, TimeUnit.MILLISECONDS);
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(false, false, true);
	}
}
