package io.crnk.rs.internal.legacy.provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.container.ContainerRequestContext;

public class ContainerRequestContextProvider implements RequestContextParameterProvider {

	@Override
	public ContainerRequestContext provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper) {
		return requestContext;
	}

	@Override
	public boolean provides(Parameter parameter) {
		return ContainerRequestContext.class.isAssignableFrom(parameter.getType());
	}
}
