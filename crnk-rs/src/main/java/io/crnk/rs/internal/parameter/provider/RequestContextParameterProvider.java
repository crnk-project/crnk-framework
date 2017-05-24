package io.crnk.rs.internal.parameter.provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.container.ContainerRequestContext;

public interface RequestContextParameterProvider {

	<T> T provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper);

	boolean provides(Parameter parameter);
}
