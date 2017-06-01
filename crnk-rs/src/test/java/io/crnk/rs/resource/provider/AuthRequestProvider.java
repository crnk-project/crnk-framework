package io.crnk.rs.resource.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.rs.internal.legacy.provider.Parameter;
import io.crnk.rs.internal.legacy.provider.RequestContextParameterProvider;

import javax.ws.rs.container.ContainerRequestContext;

public class AuthRequestProvider implements RequestContextParameterProvider {
	@Override
	public AuthRequest provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper) {
		return AuthRequest.fromAuthorizationHeader(requestContext.getHeaderString("Authorization"));
	}

	@Override
	public boolean provides(Parameter parameter) {
		return AuthRequest.class.isAssignableFrom(parameter.getType());
	}
}
