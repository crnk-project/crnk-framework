package io.crnk.rs.internal.parameterProvider.provider;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.CookieParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import java.io.IOException;

public class CookieParamProvider implements RequestContextParameterProvider {

	@Override
	public Object provideValue(Parameter parameter, ContainerRequestContext requestContext, ObjectMapper objectMapper) {
		Object returnValue;
		String cookieName = parameter.getAnnotation(CookieParam.class).value();
		Cookie cookie = requestContext.getCookies().get(cookieName);
		if (cookie == null) {
			return null;
		} else {
			if (Cookie.class.isAssignableFrom(parameter.getType())) {
				returnValue = cookie;
			} else if (String.class.isAssignableFrom(parameter.getType())) {
				returnValue = cookie.getValue();
			} else {
				try {
					returnValue = objectMapper.readValue(cookie.getValue(), parameter.getType());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return returnValue;
	}

	@Override
	public boolean provides(Parameter parameter) {
		return parameter.isAnnotationPresent(CookieParam.class);
	}
}
