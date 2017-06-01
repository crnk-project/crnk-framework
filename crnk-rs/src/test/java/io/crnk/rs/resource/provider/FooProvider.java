package io.crnk.rs.resource.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.rs.internal.legacy.provider.Parameter;
import io.crnk.rs.internal.legacy.provider.RequestContextParameterProvider;

import javax.ws.rs.container.ContainerRequestContext;

public class FooProvider implements RequestContextParameterProvider {

	@Override
	public String provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper) {
		return "foo";
	}

	@Override
	public boolean provides(Parameter parameter) {
		return parameter.isAnnotationPresent(Foo.class);
	}
}
