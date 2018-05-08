package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.exception.RequestBodyException;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a controller contract. There can be many kinds of requests that can be send to the framework. The
 * initial process of checking if a request is acceptable is managed by
 * {@link BaseController#isAcceptable(JsonPath, String)} method. If the method returns
 * true, the matched controller is used to handle the request.
 */
public abstract class BaseController implements Controller {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected ControllerContext context;

	@Override
	public void init(ControllerContext context) {
		this.context = context;
	}

	/**
	 * Passes the request to controller method.
	 *
	 * @param jsonPath          Requested resource path
	 * @param queryAdapter      QueryAdapter
	 * @param parameterProvider repository method legacy provider
	 * @param requestDocument   Top-level JSON object from method's body of the request passed as {@link Document}
	 * @return BaseResponseContext object
	 * @deprecated in favor of {@link #handleAsync(JsonPath, QueryAdapter, RepositoryMethodParameterProvider, Document)}
	 */
	@Override
	@Deprecated
	public final Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider
			parameterProvider, Document requestDocument) {

		Result<Response> response = handleAsync(jsonPath, queryAdapter, parameterProvider, requestDocument);
		PreconditionUtil.assertNotNull("no response by controller provided", response);
		return response.get();
	}


	protected void verifyTypes(HttpMethod methodType, RegistryEntry endpointRegistryEntry,
							   RegistryEntry bodyRegistryEntry) {
		if (endpointRegistryEntry.equals(bodyRegistryEntry)) {
			return;
		}
		if (bodyRegistryEntry == null || !bodyRegistryEntry.isParent(endpointRegistryEntry)) {
			String message = String.format("Inconsistent type definition between path and body: body type: " +
					"%s, request type: %s", bodyRegistryEntry.getResourceInformation().getResourceType(), endpointRegistryEntry.getResourceInformation().getResourceType());
			throw new RequestBodyException(methodType, endpointRegistryEntry.getResourceInformation().getResourceType(), message);
		}
	}

	protected RegistryEntry getRegistryEntry(String resource) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry registryEntry = resourceRegistry.getEntry(resource);
		if (registryEntry == null) {
			throw new RepositoryNotFoundException(resource);
		}
		return registryEntry;
	}

	protected RegistryEntry getRegistryEntryByPath(String resourcePath) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry registryEntry = resourceRegistry.getEntryByPath(resourcePath);
		if (registryEntry == null) {
			throw new RepositoryNotFoundException(resourcePath);
		}
		return registryEntry;
	}
}
