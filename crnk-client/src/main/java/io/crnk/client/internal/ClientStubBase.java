package io.crnk.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.client.ResponseBodyException;
import io.crnk.core.queryspec.QuerySpecSerializer;
import io.crnk.client.ClientException;
import io.crnk.client.CrnkClient;
import io.crnk.client.TransportException;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.client.response.JsonLinksInformation;
import io.crnk.client.response.JsonMetaInformation;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.utils.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ClientStubBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientStubBase.class);

	protected CrnkClient client;

	protected JsonApiUrlBuilder urlBuilder;

	protected Class<?> resourceClass;

	public ClientStubBase(CrnkClient client, JsonApiUrlBuilder urlBuilder, Class<?> resourceClass) {
		this.client = client;
		this.urlBuilder = urlBuilder;
		this.resourceClass = resourceClass;
	}

	protected Object executeGet(String requestUrl, ResponseType responseType) {
		return execute(requestUrl, responseType, HttpMethod.GET, null);
	}

	protected Object executeDelete(String requestUrl) {
		return execute(requestUrl, ResponseType.NONE, HttpMethod.DELETE, null);
	}

	protected Object execute(String url, ResponseType responseType, HttpMethod method, String requestBody) {
		try {

			HttpAdapter httpAdapter = client.getHttpAdapter();
			HttpAdapterRequest request = httpAdapter.newRequest(url, method, requestBody);

			LOGGER.debug("requesting {} {}", method, url);
			if (requestBody != null) {
				LOGGER.debug("request body: {}", requestBody);
			}

			if (method == HttpMethod.POST || method == HttpMethod.PATCH) {
				request.header("Content-Type", HttpHeaders.JSONAPI_CONTENT_TYPE + "; charset=" +
						HttpHeaders.DEFAULT_CHARSET);
			}
			request.header("Accept", HttpHeaders.JSONAPI_CONTENT_TYPE);

			HttpAdapterResponse response = request.execute();

			if (!response.isSuccessful()) {
				throw handleError(response);
			}

			String body = response.body();
			LOGGER.debug("response body: {}", body);
			ObjectMapper objectMapper = client.getObjectMapper();

			if (responseType != ResponseType.NONE) {
				if (body.length() == 0) {
					throw new ResponseBodyException("no body received");
				}
				if (Resource.class.equals(resourceClass)) {
					Document document = objectMapper.readValue(body, Document.class);
					return toResourceResponse(document, objectMapper);
				} else {
					Document document = objectMapper.readValue(body, Document.class);

					ClientDocumentMapper documentMapper = client.getDocumentMapper();
					return documentMapper.fromDocument(document, responseType == ResponseType.RESOURCES);
				}
			}
			return null;
		} catch (IOException e) {
			throw new TransportException(e);
		}
	}

	private static Object toResourceResponse(Document document, ObjectMapper objectMapper) {
		Object data = document.getData().get();
		if (data instanceof List) {
			DefaultResourceList<Resource> list = new DefaultResourceList<>();
			list.addAll((List<Resource>) data);
			if (document.getMeta() != null) {
				list.setMeta(new JsonMetaInformation(document.getMeta(), objectMapper));
			}
			if (document.getLinks() != null) {
				list.setLinks(new JsonLinksInformation(document.getMeta(), objectMapper));
			}
			return list;
		} else {
			return data;
		}
	}

	protected RuntimeException handleError(HttpAdapterResponse response) throws IOException {
		ErrorResponse errorResponse = null;
		String body = response.body();
		String contentType = response.getResponseHeader(HttpHeaders.HTTP_CONTENT_TYPE);
		if (body.length() > 0 && contentType != null && contentType.toLowerCase().contains(HttpHeaders.JSONAPI_CONTENT_TYPE)) {

			ObjectMapper objectMapper = client.getObjectMapper();
			Document document = objectMapper.readValue(body, Document.class);
			if (document.getErrors() != null && !document.getErrors().isEmpty()) {
				errorResponse = new ErrorResponse(document.getErrors(), response.code());
			}
		}
		if (errorResponse == null) {
			errorResponse = new ErrorResponse(null, response.code());
		}

		ExceptionMapperRegistry exceptionMapperRegistry = client.getExceptionMapperRegistry();
		Optional<ExceptionMapper<?>> mapper = (Optional) exceptionMapperRegistry.findMapperFor(errorResponse);
		if (mapper.isPresent()) {
			Throwable throwable = mapper.get().fromErrorResponse(errorResponse);
			if (throwable instanceof RuntimeException) {
				return (RuntimeException) throwable;
			} else {
				return new ClientException(response.code(), response.message(), throwable);
			}
		} else {
			return new ClientException(response.code(), response.message());
		}
	}

	public enum ResponseType {
		NONE, RESOURCE, RESOURCES
	}

	public QuerySpecSerializer getQuerySpecSerializer() {
		return this.urlBuilder.getQuerySpecSerializer();
	}

	public void setQuerySpecSerializer(QuerySpecSerializer querySpecSerializer) {
		this.urlBuilder.setQuerySpecSerializer(querySpecSerializer);
	}

}
