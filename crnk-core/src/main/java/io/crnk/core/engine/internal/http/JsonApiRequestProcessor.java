package io.crnk.core.engine.internal.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.*;
import io.crnk.core.engine.internal.dispatcher.path.ActionPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.exception.CrnkExceptionMapper;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.exception.CrnkMappableException;
import io.crnk.core.exception.CrnkMatchingException;
import io.crnk.core.module.Module;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class JsonApiRequestProcessor implements HttpRequestProcessor {

	public static final String JSONAPI_CONTENT_TYPE = "application/vnd.api+json";
	private static final Logger LOGGER = LoggerFactory.getLogger(JsonApiRequestProcessor.class);
	private Module.ModuleContext moduleContext;

	public JsonApiRequestProcessor(Module.ModuleContext moduleContext) {
		this.moduleContext = moduleContext;
	}

	public static boolean isJsonApiRequest(HttpRequestContext requestContext) {
		if (requestContext.getMethod().equalsIgnoreCase(HttpMethod.PATCH.toString()) || requestContext.getMethod()
				.equalsIgnoreCase(HttpMethod.POST.toString())) {
			String contentType = requestContext.getRequestHeader(HttpHeaders.HTTP_CONTENT_TYPE);
			if (contentType == null || !contentType.startsWith(JSONAPI_CONTENT_TYPE)) {
				return false;
			}
		}
		boolean acceptsJsonApi = requestContext.accepts(JSONAPI_CONTENT_TYPE);
		boolean acceptsAny = requestContext.acceptsAny();
		return acceptsJsonApi || acceptsAny;
	}

	@Override
	public void process(HttpRequestContext requestContext) throws IOException {
		if (isJsonApiRequest(requestContext)) {

			ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
			RequestDispatcher requestDispatcher = moduleContext.getRequestDispatcher();

			ServiceUrlProvider serviceUrlProvider = resourceRegistry.getServiceUrlProvider();
			try {
				String path = requestContext.getPath();

				if (serviceUrlProvider instanceof HttpRequestContextProvider) {
					((HttpRequestContextProvider) serviceUrlProvider).onRequestStarted(requestContext);
				}

				JsonPath jsonPath = new PathBuilder(resourceRegistry).build(path);
				Map<String, Set<String>> parameters = requestContext.getRequestParameters();
				String method = requestContext.getMethod();

				if (jsonPath instanceof ActionPath) {
					// inital implementation, has to improve
					requestDispatcher.dispatchAction(path, method, parameters);
				} else if (jsonPath != null) {
					byte[] requestBody = requestContext.getRequestBody();

					Document document = null;
					if (requestBody != null && requestBody.length > 0) {
						ObjectMapper objectMapper = moduleContext.getObjectMapper();
						document = objectMapper.readerFor(Document.class).readValue(requestBody);
					}

					RepositoryMethodParameterProvider parameterProvider = requestContext.getRequestParameterProvider();
					Response crnkResponse = requestDispatcher
							.dispatchRequest(path, method, parameters, parameterProvider, document);
					setResponse(requestContext, crnkResponse);
				} else {
					// no repositories invoked, we do nothing
				}

			} catch (CrnkMappableException e) {
				// log error in CrnkMappableException mapper.
				Response
						crnkResponse = new CrnkExceptionMapper().toErrorResponse(e).toResponse();
				setResponse(requestContext, crnkResponse);
			} catch (CrnkMatchingException e) {
				LOGGER.warn("failed to process request", e);
			}

		}
	}

	private void setResponse(HttpRequestContext requestContext, Response crnkResponse)
			throws IOException {
		if (crnkResponse != null) {
			ObjectMapper objectMapper = moduleContext.getObjectMapper();
			String responseBody = objectMapper.writeValueAsString(crnkResponse.getDocument());

			requestContext.setResponse(crnkResponse.getHttpStatus(), responseBody);

			String contentType = JSONAPI_CONTENT_TYPE;
			requestContext.setResponseHeader("Content-Type", contentType);
		}
	}
}
