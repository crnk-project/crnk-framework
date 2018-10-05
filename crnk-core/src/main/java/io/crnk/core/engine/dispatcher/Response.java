package io.crnk.core.engine.dispatcher;

import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.http.HttpStatus;

public class Response {

	private Integer httpStatus;

	private Document document;

	public Response(Document document, Integer statusCode) {
		super();
		this.httpStatus = statusCode;
		this.document = document;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(Integer httpStatus) {
		this.httpStatus = httpStatus;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	@Override
	public int hashCode() {
		return Objects.hash(document, httpStatus);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Response)) {
			return false;
		}
		Response other = (Response) obj;
		return Objects.equals(document, other.document) && Objects.equals(httpStatus, other.httpStatus);
	}

	public HttpResponse toHttpResponse(ObjectMapper objectMapper) {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setStatusCode(getHttpStatus());

		if (getHttpStatus() != HttpStatus.NO_CONTENT_204) {
			String responseBody;
			try {
				responseBody = objectMapper.writeValueAsString(getDocument());
			}
			catch (JsonProcessingException e) {
				throw new IllegalStateException(e);
			}
			httpResponse.setBody(responseBody);
			httpResponse.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET);
		}
		return httpResponse;
	}
}
