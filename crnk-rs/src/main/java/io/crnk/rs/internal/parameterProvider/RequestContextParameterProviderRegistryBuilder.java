package io.crnk.rs.internal.parameterProvider;

import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.rs.internal.parameterProvider.provider.*;

import java.util.HashSet;
import java.util.Set;

public class RequestContextParameterProviderRegistryBuilder {

	private Set<RequestContextParameterProvider> providers = new HashSet<>();

	public RequestContextParameterProviderRegistry build(ServiceDiscovery discovery) {
		addDefaultProviders();
		for (RequestContextParameterProvider parameterProvider : discovery.getInstancesByType(RequestContextParameterProvider.class)) {
			registerRequestContextProvider(parameterProvider);
		}
		return new RequestContextParameterProviderRegistry(providers);
	}

	private void addDefaultProviders() {
		registerRequestContextProvider(new ContainerRequestContextProvider());
		registerRequestContextProvider(new SecurityContextProvider());
		registerRequestContextProvider(new CookieParamProvider());
		registerRequestContextProvider(new HeaderParamProvider());
		registerRequestContextProvider(new QueryParamProvider());
	}

	private void registerRequestContextProvider(RequestContextParameterProvider requestContextParameterProvider) {
		providers.add(requestContextParameterProvider);
	}
}
