package io.crnk.client.action;

import io.crnk.core.engine.url.ServiceUrlProvider;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

public class JerseyActionStubFactory implements ActionStubFactory {

	private Client client;

	private ActionStubFactoryContext context;

	private JerseyActionStubFactory() {
	}

	public static JerseyActionStubFactory newInstance() {
		return newInstance(ClientBuilder.newClient());
	}

	public static JerseyActionStubFactory newInstance(Client client) {
		JerseyActionStubFactory factory = new JerseyActionStubFactory();
		factory.client = client;
		return factory;
	}

	@Override
	public void init(ActionStubFactoryContext context) {
		this.context = context;
	}

	@Override
	public <T> T createStub(Class<T> interfaceClass) {
		ServiceUrlProvider serviceUrlProvider = context.getServiceUrlProvider();
		String serviceUrl = serviceUrlProvider.getUrl();

		WebTarget target = client.target(serviceUrl);
		return WebResourceFactory.newResource(interfaceClass, target);
	}

}
