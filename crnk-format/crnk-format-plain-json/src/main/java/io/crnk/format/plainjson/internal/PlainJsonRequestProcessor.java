package io.crnk.format.plainjson.internal;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import io.crnk.core.module.Module;
import io.crnk.core.utils.Prioritizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlainJsonRequestProcessor extends JsonApiRequestProcessor implements HttpRequestProcessor, Prioritizable {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlainJsonRequestProcessor.class);

	public PlainJsonRequestProcessor(Module.ModuleContext moduleContext) {
		super(moduleContext);
	}


	public static boolean isPlainJsonRequest(HttpRequestContext requestContext, boolean acceptPlainJson) {
		String method = requestContext.getMethod().toUpperCase();
		boolean isPatch = method.equals(HttpMethod.PATCH.toString());
		boolean isPost = method.equals(HttpMethod.POST.toString());
		if (isPatch || isPost) {
			String contentType = requestContext.getRequestHeader(HttpHeaders.HTTP_CONTENT_TYPE);
			if (contentType == null || !contentType.startsWith(HttpHeaders.JSONAPI_CONTENT_TYPE)) {
				return true;
			}
		}

		// short-circuit each of the possible Accept MIME type checks, so that we don't keep comparing after we have already
		// found a match. Intentionally kept as separate statements (instead of a big, chained ||) to ease debugging/maintenance.
		boolean acceptsJsonApi = requestContext.accepts(HttpHeaders.JSONAPI_CONTENT_TYPE);
		boolean acceptsJson = requestContext.accepts(HttpHeaders.JSON_CONTENT_TYPE);
		boolean acceptsPlainJson = !acceptsJsonApi || acceptsJson || requestContext.acceptsAny();
		LOGGER.debug("accepting request as plain json: {}", acceptPlainJson);
		return acceptsPlainJson;
	}

	@Override
	public boolean accepts(HttpRequestContext context) {
		if (isPlainJsonRequest(context, isAcceptingPlainJson())) {
			JsonPath jsonPath = getJsonPath(context);
			LOGGER.debug("resource path: {}", jsonPath);
			return jsonPath != null;
		}
		return false;
	}

	@Override
	protected Document getRequestDocument(HttpRequestContext requestContext) throws JsonProcessingException {
		byte[] requestBody = requestContext.getRequestBody();
		if (requestBody != null && requestBody.length > 0) {
			ObjectMapper objectMapper = moduleContext.getObjectMapper();
			try {
				return objectMapper.readerFor(PlainJsonDocument.class).readValue(requestBody);
			}
			catch (JsonProcessingException e) {
				throw e;
			}
			catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return null;
	}

	@Override
	protected String getContentType() {
		return HttpHeaders.JSON_CONTENT_TYPE;
	}

	@Override
	protected HttpResponse toHttpResponse(Response response) {
		Document document = response.getDocument();
		if (document != null) {
			response.setDocument(new PlainJsonDocument(document));
		}
		return super.toHttpResponse(response);
	}


	@Override
	public int getPriority() {
		return -100;
	}
}