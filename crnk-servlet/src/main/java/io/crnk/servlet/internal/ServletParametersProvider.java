package io.crnk.servlet.internal;

import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class ServletParametersProvider implements RepositoryMethodParameterProvider {

	private ServletContext servletContext;
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;

	public ServletParametersProvider(ServletContext servletContext, HttpServletRequest httpServletRequest,
									 HttpServletResponse httpServletResponse) {
		this.servletContext = servletContext;
		this.httpServletRequest = httpServletRequest;
		this.httpServletResponse = httpServletResponse;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T provide(Method method, int parameterIndex) {
		Class<?> parameterType = method.getParameterTypes()[parameterIndex];
		Object returnValue = null;
		if (ServletContext.class.isAssignableFrom(parameterType)) {
			returnValue = servletContext;
		} else if (HttpServletRequest.class.isAssignableFrom(parameterType)) {
			returnValue = httpServletRequest;
		} else if (HttpServletResponse.class.isAssignableFrom(parameterType)) {
			returnValue = httpServletResponse;
		}
		return (T) returnValue;
	}
}
