package io.crnk.core.engine.internal.http;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonApiRequestProcessorBase {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected Module.ModuleContext moduleContext;

	private Boolean acceptingPlainJson;

	protected JsonApiRequestProcessorHelper helper;

	public JsonApiRequestProcessorBase(Module.ModuleContext moduleContext) {
		this.moduleContext = moduleContext;
		helper = new JsonApiRequestProcessorHelper(moduleContext);
	}

	protected boolean isAcceptingPlainJson() {
		if (acceptingPlainJson == null) {
			acceptingPlainJson =
					!Boolean.parseBoolean(moduleContext.getPropertiesProvider().getProperty(CrnkProperties.REJECT_PLAIN_JSON));
		}
		return acceptingPlainJson;
	}

	protected HttpResponse getErrorResponse(JsonProcessingException e) {
		final String message = "Json Parsing failed";
		Response response = buildBadRequestResponse(message, e.getMessage());
		logger.error(message, e);
		return toHttpResponse(response);
	}

	protected HttpResponse buildMethodNotAllowedResponse(final String method) {
		Document responseDocument = new Document();
		responseDocument.setErrors(Arrays.asList(MethodNotAllowedException.createErrorData(method)));
		Response response = new Response(responseDocument, HttpStatus.METHOD_NOT_ALLOWED_405);
		logger.warn("method not allowed: {}", method);
		return toHttpResponse(response);
	}

	protected HttpResponse toHttpResponse(Response response) {
		ObjectMapper objectMapper = moduleContext.getObjectMapper();
		HttpResponse httpResponse = response.toHttpResponse(objectMapper, getContentType());

		logger.debug("setup http resposne {}", httpResponse);
		return httpResponse;
	}

	protected String getContentType() {
		return HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET;
	}

	protected Document getRequestDocument(HttpRequestContext requestContext) throws JsonProcessingException {
		byte[] requestBody = requestContext.getRequestBody();
		if (requestBody != null && requestBody.length > 0) {
			ObjectMapper objectMapper = moduleContext.getObjectMapper();
			try {
				return objectMapper.readerFor(Document.class).readValue(requestBody);
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


	protected Response buildBadRequestResponse(final String message, final String detail) {
		Document responseDocument = new Document();
		responseDocument.setErrors(Arrays.asList(ErrorData.builder()
				.setStatus(String.valueOf(400))
				.setTitle(message)
				.setDetail(detail)
				.build()));
		return new Response(responseDocument, 400);
	}
}
