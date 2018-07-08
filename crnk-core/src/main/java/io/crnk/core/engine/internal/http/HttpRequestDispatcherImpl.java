package io.crnk.core.engine.internal.http;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.http.*;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that can be used to integrate Crnk with external frameworks like Jersey, Spring etc. See crnk-rs
 * and crnk-servlet for usage.
 */
public class HttpRequestDispatcherImpl implements RequestDispatcher {


	private final ExceptionMapperRegistry exceptionMapperRegistry;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ModuleRegistry moduleRegistry;

	public HttpRequestDispatcherImpl(ModuleRegistry moduleRegistry, ExceptionMapperRegistry exceptionMapperRegistry) {
		this.moduleRegistry = moduleRegistry;
		this.exceptionMapperRegistry = exceptionMapperRegistry;

		// TODO clean this class up
		this.moduleRegistry.setRequestDispatcher(this);
	}

	@Override
	public Optional<Result<HttpResponse>> process(HttpRequestContextBase requestContextBase) throws IOException {
		HttpRequestContextBaseAdapter requestContext = new HttpRequestContextBaseAdapter(requestContextBase);

		QueryContext queryContext = requestContext.getQueryContext();

		HttpRequestContextProvider httpRequestContextProvider = moduleRegistry.getHttpRequestContextProvider();
		try {
			httpRequestContextProvider.onRequestStarted(requestContext);

			ServiceUrlProvider serviceUrlProvider = moduleRegistry.getResourceRegistry().getServiceUrlProvider();
			if (serviceUrlProvider != null) {
				queryContext.setBaseUrl(serviceUrlProvider.getUrl());
			}

			List<HttpRequestProcessor> processors = moduleRegistry.getHttpRequestProcessors();
			PreconditionUtil.verify(!processors.isEmpty(), "no processors available");
			for (HttpRequestProcessor processor : processors) {
				if (processor.supportsAsync()) {
					if (processor.accepts(requestContext)) {
						logger.debug("using async processor {}", processor);
						Result<HttpResponse> response = processor.processAsync(requestContext);
						if (response == null) {
							// e.g. actions that are forward, not that clean, but limited by integrations (like JAXRS)
							break;
						}

						// attach request context to subscriber context
						response = httpRequestContextProvider.attach(response);

						return Optional.of(response.doWork(it -> requestContext.setResponse(it)));

					}

				} else {
					logger.debug("using sync processor {}", processor);
					processor.process(requestContext);
					if (requestContext.hasResponse()) {
						return Optional.of(new ImmediateResult<>(requestContext.getResponse()));
					}
				}
			}
			logger.debug("no processor found for request {}", requestContextBase);
			return Optional.empty();
		} finally {
			httpRequestContextProvider.onRequestFinished();
		}
	}

	@Override
	@Deprecated
	public Response dispatchRequest(String path, String method, Map<String, Set<String>> parameters,
									RepositoryMethodParameterProvider parameterProvider,
									Document requestBody) {

		List<HttpRequestProcessor> processors = moduleRegistry.getHttpRequestProcessors();
		JsonApiRequestProcessor processor = (JsonApiRequestProcessor) processors.stream()
				.filter(it -> it instanceof JsonApiRequestProcessor).findFirst().get();

		JsonPath jsonPath = new PathBuilder(moduleRegistry.getResourceRegistry(), moduleRegistry.getTypeParser()).build(path);

		HttpRequestContext requestContext = moduleRegistry.getHttpRequestContextProvider().getRequestContext();
		QueryContext queryContext = requestContext.getQueryContext();
		return processor.processAsync(jsonPath, method, parameters,
				parameterProvider, requestBody, queryContext).get();
	}


	@Override
	public void dispatchAction(String path, String method, Map<String, Set<String>> parameters) {
		JsonPath jsonPath = new PathBuilder(moduleRegistry.getResourceRegistry(), moduleRegistry.getTypeParser()).build(path);

		// preliminary implementation, more to come in the future
		ActionFilterChain chain = new ActionFilterChain();

		DocumentFilterContextImpl context = new DocumentFilterContextImpl(jsonPath, null, null, null, method);
		chain.doFilter(context);
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
}
