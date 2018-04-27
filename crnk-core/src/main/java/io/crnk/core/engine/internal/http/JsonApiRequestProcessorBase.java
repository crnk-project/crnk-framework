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
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonApiRequestProcessorBase {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected Module.ModuleContext moduleContext;

	private Boolean acceptingPlainJson;

	protected QueryAdapterBuilder queryAdapterBuilder;

	protected ControllerRegistry controllerRegistry;

	public JsonApiRequestProcessorBase(Module.ModuleContext moduleContext, QueryAdapterBuilder queryAdapterBuilder,
			ControllerRegistry controllerRegistry) {
		this.moduleContext = moduleContext;
		this.queryAdapterBuilder = queryAdapterBuilder;
		this.controllerRegistry = controllerRegistry;
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

		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setStatusCode(response.getHttpStatus());

		if (response.getHttpStatus() != HttpStatus.NO_CONTENT_204) {
			String responseBody;
			try {
				responseBody = objectMapper.writeValueAsString(response.getDocument());
			}
			catch (JsonProcessingException e) {
				throw new IllegalStateException(e);
			}
			httpResponse.setBody(responseBody);
			httpResponse.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET);
		}
		logger.debug("setup http resposne {}", httpResponse);
		return httpResponse;
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


	protected JsonPath getJsonPath(HttpRequestContext requestContext) {
		String path = requestContext.getPath();
		ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
		return new PathBuilder(resourceRegistry).build(path);
	}

	protected ResourceInformation getRequestedResource(JsonPath jsonPath) {
		ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
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
		}
		else {
			return registryEntry.getResourceInformation();
		}
	}

	public void setControllerRegistry(ControllerRegistry controllerRegistry) {
		this.controllerRegistry = controllerRegistry;
	}
}
