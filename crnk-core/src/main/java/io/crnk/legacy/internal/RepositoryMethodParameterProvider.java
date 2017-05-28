package io.crnk.legacy.internal;

import java.lang.reflect.Method;

/**
 * Provides additional parameters for an annotated document method. The method can accept more than the required
 * parameters for a functionality. For example:
 * <pre>
 * {@code
 *  &#64;JsonApiFindOne
 *  public Resource findOne(Long id) {
 *    ...
 *  }
 *  }
 * </pre>
 * This method has {@link io.crnk.legacy.repository.annotations.JsonApiFindOne} annotation which require the first
 * legacy to be a document identifier to be found. However, it is not the only legacy that can be defined.
 * It's possible to pass additional, web framework dependant objects associated with a request. When using JAX-RS
 * integration, it's possible to pass <b>javax.ws.rs.core.SecurityContext</b>. To allow doing that, JAX-RS adapter
 * has implemented {@link RepositoryMethodParameterProvider} to pass several framework classes to the document method.
 * An example below shows a sample document which makes use of JAX-RS integration:
 * <pre>
 * {@code
 *  &#64;JsonApiFindOne
 *  public Resource findOne(Long id, @HeaderParam("X-Token") String auth Token, SecurityContext securityContext) {
 *    ...
 *  }
 *  }
 * </pre>
 * <p>
 * This interface has to be implemented for every Crnk web framework integration.
 * </p>
 */
public interface RepositoryMethodParameterProvider {

	/**
	 * Return an instance of a custom legacy.
	 *
	 * @param method         document method which contain the legacy
	 * @param parameterIndex index of the legacy in the method parameters
	 * @param <T>            Type of a legacy
	 * @return legacy value or null if not found
	 */
	<T> T provide(Method method, int parameterIndex);
}
