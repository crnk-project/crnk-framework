package io.crnk.rs.internal.legacy.provider;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class QueryParamProvider implements RequestContextParameterProvider {

	@Override
	public Object provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper) {
		Object returnValue;
		List<String> value =
				requestContext.getUriInfo().getQueryParameters().get(parameter.getAnnotation(QueryParam.class).value());
		if (value == null || value.isEmpty()) {
			return null;
		}
		else {
			if (String.class.isAssignableFrom(parameter.getType())) {
				// Given a query string: ?x=y&x=z, JAX-RS will return a value of y.
				returnValue = value.get(0);
			}
			else if (Iterable.class.isAssignableFrom(parameter.getType())) {
				returnValue = value;
			}
			else {
				try {
					returnValue = objectMapper.readValue(value.get(0), parameter.getType());
				}
				catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}
		return returnValue;
	}

	@Override
	public boolean provides(Parameter parameter) {
		return parameter.isAnnotationPresent(QueryParam.class);
	}
}
