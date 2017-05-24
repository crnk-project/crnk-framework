package io.crnk.rs.internal.parameter;

import io.crnk.core.utils.Optional;
import io.crnk.rs.internal.parameter.provider.Parameter;
import io.crnk.rs.internal.parameter.provider.RequestContextParameterProvider;

import java.util.Collection;
import java.util.Set;

public class RequestContextParameterProviderRegistry {

	private Set<RequestContextParameterProvider> parameterProviders;

	public RequestContextParameterProviderRegistry(Set<RequestContextParameterProvider> parameterProviders) {
		this.parameterProviders = parameterProviders;
	}

	public Optional<RequestContextParameterProvider> findProviderFor(Parameter parameter) {
		for (RequestContextParameterProvider parameterProvider : parameterProviders) {
			if (parameterProvider.provides(parameter)) {
				return Optional.of(parameterProvider);
			}
		}
		return Optional.empty();
	}

	public Collection<RequestContextParameterProvider> getParameterProviders() {
		return parameterProviders;
	}

	public void setParameterProviders(Set<RequestContextParameterProvider> parameterProviders) {
		this.parameterProviders = parameterProviders;
	}
}
