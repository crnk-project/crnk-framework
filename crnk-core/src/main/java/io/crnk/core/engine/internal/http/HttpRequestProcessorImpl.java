package io.crnk.core.engine.internal.http;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.controller.BaseController;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.utils.Optional;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.legacy.queryParams.QueryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that can be used to integrate Crnk with external frameworks like Jersey, Spring etc. See crnk-rs
 * and crnk-servlet for usage.
 */
public class HttpRequestProcessorImpl implements RequestDispatcher {

	private final ControllerRegistry controllerRegistry;
	private final ExceptionMapperRegistry exceptionMapperRegistry;
	private final ServiceUrlProvider serviceUrlProvider;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private ModuleRegistry moduleRegistry;

	private QueryAdapterBuilder queryAdapterBuilder;

	public HttpRequestProcessorImpl(ModuleRegistry moduleRegistry, ServiceUrlProvider serviceUrlProvider, ControllerRegistry controllerRegistry,
									ExceptionMapperRegistry exceptionMapperRegistry, QueryAdapterBuilder queryAdapterBuilder) {
		this.controllerRegistry = controllerRegistry;
		this.serviceUrlProvider = serviceUrlProvider;
		this.moduleRegistry = moduleRegistry;
		this.exceptionMapperRegistry = exceptionMapperRegistry;
		this.queryAdapterBuilder = queryAdapterBuilder;

		// TODO clean this class up
		this.moduleRegistry.setRequestDispatcher(this);
	}

	@Override
	public void process(HttpRequestContextBase requestContextBase) throws IOException {
		HttpRequestContextBaseAdapter requestContext = new HttpRequestContextBaseAdapter(requestContextBase);
		try {
			if (serviceUrlProvider instanceof HttpRequestContextProvider) {
				((HttpRequestContextProvider) serviceUrlProvider).onRequestStarted(requestContext);
			}

			List<HttpRequestProcessor> processors = moduleRegistry.getHttpRequestProcessors();
			PreconditionUtil.assertFalse("no processors available", processors.isEmpty());
			for (HttpRequestProcessor processor : processors) {
				processor.process(requestContext);
				if (requestContext.hasResponse()) {
					break;
				}
			}
		} finally {
			if (serviceUrlProvider instanceof HttpRequestContextProvider) {
				((HttpRequestContextProvider) serviceUrlProvider).onRequestFinished();
			}
		}
	}

	/**
	 * Dispatch the request from a client
	 *
	 * @param path              built represents the URI sent in the request
	 * @param method            type of the request e.g. POST, GET, PATCH
	 * @param parameterProvider repository method legacy provider
	 * @param requestBody       deserialized body of the client request
	 * @return the response form the Crnk
	 */
	@Override
	public Response dispatchRequest(String path, String method, Map<String, Set<String>> parameters,
									RepositoryMethodParameterProvider parameterProvider,
									Document requestBody) {

		JsonPath jsonPath = new PathBuilder(moduleRegistry.getResourceRegistry()).build(path);
		try {
			BaseController controller = controllerRegistry.getController(jsonPath, method);

			ResourceInformation resourceInformation = getRequestedResource(jsonPath);
			QueryAdapter queryAdapter = queryAdapterBuilder.build(resourceInformation, parameters);

			DefaultFilterRequestContext context = new DefaultFilterRequestContext(jsonPath, queryAdapter, parameterProvider,
					requestBody, method);
			DefaultFilterChain chain = new DefaultFilterChain(controller);
			return chain.doFilter(context);
		} catch (Exception e) {
			Optional<JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
			if (exceptionMapper.isPresent()) {
				//noinspection unchecked
				return exceptionMapper.get().toErrorResponse(e).toResponse();
			} else {
				logger.error("failed to process request", e);
				throw e;
			}
		}
	}

	private ResourceInformation getRequestedResource(JsonPath jsonPath) {
		ResourceRegistry resourceRegistry = moduleRegistry.getResourceRegistry();
		RegistryEntry registryEntry = resourceRegistry.getEntry(jsonPath.getResourceType());
		PreconditionUtil.assertNotNull("repository not found, that should have been catched earlier", registryEntry);
		String elementName = jsonPath.getElementName();
		if (elementName != null && !elementName.equals(jsonPath.getResourceType())) {
			ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(elementName);
			if (relationshipField == null) {
				throw new ResourceFieldNotFoundException(elementName);
			}
			String oppositeResourceType = relationshipField.getOppositeResourceType();
			return resourceRegistry.getEntry(oppositeResourceType).getResourceInformation();
		} else {
			return registryEntry.getResourceInformation();
		}
	}

	@Override
	public void dispatchAction(String path, String method, Map<String, Set<String>> parameters) {
		JsonPath jsonPath = new PathBuilder(moduleRegistry.getResourceRegistry()).build(path);

		// preliminary implementation, more to come in the future
		ActionFilterChain chain = new ActionFilterChain();

		DefaultFilterRequestContext context = new DefaultFilterRequestContext(jsonPath, null, null, null, method);
		chain.doFilter(context);
	}

	public QueryAdapterBuilder getQueryAdapterBuilder() {
		return queryAdapterBuilder;
	}

	class DefaultFilterChain implements DocumentFilterChain {

		protected int filterIndex = 0;

		protected BaseController controller;

		public DefaultFilterChain(BaseController controller) {
			this.controller = controller;
		}

		@Override
		public Response doFilter(DocumentFilterContext context) {
			List<DocumentFilter> filters = moduleRegistry.getFilters();
			if (filterIndex == filters.size()) {
				return controller.handle(context.getJsonPath(), context.getQueryAdapter(), context.getParameterProvider(),
						context.getRequestBody());
			} else {
				DocumentFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filter(context, this);
			}
		}
	}

	class ActionFilterChain implements DocumentFilterChain {

		protected int filterIndex = 0;


		@Override
		public Response doFilter(DocumentFilterContext context) {
			List<DocumentFilter> filters = moduleRegistry.getFilters();
			if (filterIndex == filters.size()) {
				return null;
			} else {
				DocumentFilter filter = filters.get(filterIndex);
				filterIndex++;
				return filter.filter(context, this);
			}
		}
	}

	class DefaultFilterRequestContext implements DocumentFilterContext {

		protected JsonPath jsonPath;

		protected QueryAdapter queryAdapter;

		protected RepositoryMethodParameterProvider parameterProvider;

		protected Document requestBody;

		private String method;

		public DefaultFilterRequestContext(JsonPath jsonPath, QueryAdapter queryAdapter,
										   RepositoryMethodParameterProvider parameterProvider, Document requestBody, String method) {
			this.jsonPath = jsonPath;
			this.queryAdapter = queryAdapter;
			this.parameterProvider = parameterProvider;
			this.requestBody = requestBody;
			this.method = method;
		}

		@Override
		public Document getRequestBody() {
			return requestBody;
		}

		@Override
		public RepositoryMethodParameterProvider getParameterProvider() {
			return parameterProvider;
		}

		@Override
		public QueryParams getQueryParams() {
			return ((QueryParamsAdapter) queryAdapter).getQueryParams();
		}

		@Override
		public QueryAdapter getQueryAdapter() {
			return queryAdapter;
		}

		@Override
		public JsonPath getJsonPath() {
			return jsonPath;
		}

		@Override
		public String getMethod() {
			return method;
		}
	}
}
