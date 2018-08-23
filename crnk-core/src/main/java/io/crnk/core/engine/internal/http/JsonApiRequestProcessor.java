package io.crnk.core.engine.internal.http;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.controller.Controller;
import io.crnk.core.engine.internal.dispatcher.path.ActionPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.result.ImmediateResultFactory;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.module.Module;
import io.crnk.core.utils.Optional;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonApiRequestProcessor extends JsonApiRequestProcessorBase implements HttpRequestProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonApiRequestProcessor.class);


	public JsonApiRequestProcessor(Module.ModuleContext moduleContext, ControllerRegistry controllerRegistry,
			QueryAdapterBuilder queryAdapterBuilder) {
		super(moduleContext, queryAdapterBuilder, controllerRegistry);
	}

	/**
	 * Determines whether the supplied HTTP request is considered a JSON-API request.
	 *
	 * @param requestContext The HTTP request
	 * @param acceptPlainJson Whether a plain JSON request should also be considered a JSON-API request
	 * @return <code>true</code> if it is a JSON-API request; <code>false</code> otherwise
	 * @since 2.4
	 */
	@SuppressWarnings("UnnecessaryLocalVariable")
	public static boolean isJsonApiRequest(HttpRequestContext requestContext, boolean acceptPlainJson) {
		String method = requestContext.getMethod().toUpperCase();
		boolean isPatch = method.equals(HttpMethod.PATCH.toString());
		boolean isPost = method.equals(HttpMethod.POST.toString());
		if (isPatch || isPost) {
		    //TODO validation if versioned content_type
			String contentType = requestContext.getRequestHeader(HttpHeaders.HTTP_CONTENT_TYPE);
			if (contentType == null) {
				LOGGER.warn("not a JSON-API request due to content type {}", contentType);
				return false;
			}
		}

		// short-circuit each of the possible Accept MIME type checks, so that we don't keep comparing after we have already
		// found a match. Intentionally kept as separate statements (instead of a big, chained ||) to ease debugging/maintenance.
		boolean acceptsJsonApi = requestContext.accepts(HttpHeaders.JSONAPI_CONTENT_TYPE);
		boolean acceptsAny = acceptsJsonApi || requestContext.acceptsAny();

		boolean acceptsPlainJson = acceptsAny || (acceptPlainJson && requestContext.accepts("application/json"));
		LOGGER.debug("accepting request as JSON-API: {}", acceptPlainJson);
		return acceptsPlainJson;
	}


	@Override
	public boolean supportsAsync() {
		return true;
	}

	@Override
	public boolean accepts(HttpRequestContext context) {
		if (isJsonApiRequest(context, isAcceptingPlainJson())) {
			JsonPath jsonPath = getJsonPath(context);
			LOGGER.debug("resource path: {}", jsonPath);
			return jsonPath != null;
		}
		return false;
	}

	@Override
	public Result<HttpResponse> processAsync(HttpRequestContext requestContext) {
		Result<HttpResponse> response = checkMethod(requestContext);
		if (response != null) {
			return response;
		}

		String method = requestContext.getMethod();
		ResultFactory resultFactory = moduleContext.getResultFactory();
		String path = requestContext.getPath();
		JsonPath jsonPath = getJsonPath(requestContext);
		LOGGER.debug("processing JSON API request path={}, method={}", jsonPath, method);
		Map<String, Set<String>> parameters = requestContext.getRequestParameters();
		RepositoryMethodParameterProvider parameterProvider = requestContext.getRequestParameterProvider();

		if (jsonPath instanceof ActionPath) {
			// inital implementation, has to improve
			RequestDispatcher requestDispatcher = moduleContext.getRequestDispatcher();
			requestDispatcher.dispatchAction(path, method, parameters);
			return null;
		}
		else if (jsonPath != null) {
			Document requestDocument;
			try {
				requestDocument = getRequestDocument(requestContext);
			}
			catch (JsonProcessingException e) {
				return resultFactory.just(getErrorResponse(e));
			}
			QueryContext queryContext = requestContext.getQueryContext();
			return processAsync(jsonPath, method, parameters, parameterProvider, requestDocument, queryContext)
					.map(this::toHttpResponse);
		}
		return resultFactory.just(buildMethodNotAllowedResponse(method));
	}

	private Result<HttpResponse> checkMethod(HttpRequestContext requestContext) {
		String method = requestContext.getMethod();
		boolean isPatch = method.equals(HttpMethod.PATCH.toString());
		boolean isPost = method.equals(HttpMethod.POST.toString());
		boolean isGet = method.equals(HttpMethod.GET.toString());
		boolean isDelete = method.equals(HttpMethod.DELETE.toString());
		boolean acceptsMethod = isGet || isDelete || isPatch || isPost;
		if (!acceptsMethod) {
			ResultFactory resultFactory = moduleContext.getResultFactory();
			return resultFactory.just(buildMethodNotAllowedResponse(method));
		}
		return null;
	}


	public Result<Response> processAsync(JsonPath jsonPath, String method, Map<String, Set<String>> parameters,
			RepositoryMethodParameterProvider parameterProvider,
			Document requestDocument, QueryContext queryContext) {
		try {
			ResultFactory resultFactory = moduleContext.getResultFactory();
			ResourceInformation resourceInformation = getRequestedResource(jsonPath);
			QueryAdapter queryAdapter = queryAdapterBuilder.build(resourceInformation, parameters, queryContext);

			if (resultFactory instanceof ImmediateResultFactory) {
				LOGGER.debug("processing synchronously");
				// not that document filters are not compatible with async programming
				DocumentFilterContextImpl filterContext =
						new DocumentFilterContextImpl(jsonPath, queryAdapter, parameterProvider, requestDocument, method);
				try {
					DocumentFilterChain filterChain = getFilterChain(jsonPath, method);
					Response response = filterChain.doFilter(filterContext);
					return resultFactory.just(response);
				}
				catch (Exception e) {
					Response response = toErrorResponse(e);
					return resultFactory.just(response);
				}
			}
			else {
				LOGGER.debug("processing asynchronously");
				Controller controller = controllerRegistry.getController(jsonPath, method);
				Result<Response> responseResult =
						controller.handleAsync(jsonPath, queryAdapter, parameterProvider, requestDocument);
				return responseResult.onErrorResume(this::toErrorResponse);
			}
		}
		catch (Exception e) {
			ResultFactory resultFactory = moduleContext.getResultFactory();
			return resultFactory.just(toErrorResponse(e));
		}
	}

	private Response toErrorResponse(Throwable e) {
		ExceptionMapperRegistry exceptionMapperRegistry = moduleContext.getExceptionMapperRegistry();
		Optional<JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
		if (!exceptionMapper.isPresent()) {
			LOGGER.error("failed to process request, unknown exception thrown", e);
			e = new InternalServerErrorException(e.getMessage());
			exceptionMapper = exceptionMapperRegistry.findMapperFor(e.getClass());
			PreconditionUtil
					.assertTrue("no exception mapper for InternalServerErrorException found", exceptionMapper.isPresent());
		}
		else {
			LOGGER.debug("dispatching exception to mapper", e);
		}
		return exceptionMapper.get().toErrorResponse(e).toResponse();
	}

	protected DocumentFilterChain getFilterChain(JsonPath jsonPath, String method) {
		Controller controller = controllerRegistry.getController(jsonPath, method);
		return new DocumentFilterChainImpl(moduleContext, controller);

	}

}
