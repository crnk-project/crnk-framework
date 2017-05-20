package io.crnk.rs.internal.parameterProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.utils.Optional;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.rs.internal.parameterProvider.provider.Parameter;
import io.crnk.rs.internal.parameterProvider.provider.RequestContextParameterProvider;

import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Method;

/**
 * <p>
 * An implementation of parameter provider for JAX-RS integration based on a registry of RequestContextParameterProvider
 * provided by an instance of RequestContextParameterProviderRegistry.
 * By default, the registry supports the following parameters:
 * </p>
 * <ol>
 * <li>{@link ContainerRequestContext}</li>
 * <li>{@link SecurityContext}</li>
 * <li>Values annotated with {@link CookieParam}</li>
 * <li>Values annotated with {@link HeaderParam}</li>
 * <li>Values annotated with {@link QueryParam}</li>
 * </ol>
 * <p>
 * Value casting for values annotated with {@link CookieParam} and {@link HeaderParam} does <b>not</b> conform with the
 * definitions described in the JAX-RS specification. If a value is not String or {@link Cookie} for
 * {@link CookieParam}, an instance of {@link ObjectMapper} is used to map the value to the desired type.
 * </p>
 */
public class JaxRsParameterProvider implements RepositoryMethodParameterProvider {

	private final ObjectMapper objectMapper;
	private final ContainerRequestContext requestContext;
	private final RequestContextParameterProviderRegistry parameterProviderRegistry;

	public JaxRsParameterProvider(ObjectMapper objectMapper, ContainerRequestContext requestContext, RequestContextParameterProviderRegistry parameterProviderRegistry) {
		this.objectMapper = objectMapper;
		this.requestContext = requestContext;
		this.parameterProviderRegistry = parameterProviderRegistry;
	}

	@Override
	public <T> T provide(Method method, int parameterIndex) {
		Parameter parameter = new Parameter(method, parameterIndex);
		Optional<RequestContextParameterProvider> provider = parameterProviderRegistry.findProviderFor(parameter);
		return provider.isPresent() ? (T) provider.get().provideValue(parameter, requestContext, objectMapper) : null;
	}
}
