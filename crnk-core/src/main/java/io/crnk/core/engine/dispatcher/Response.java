package io.crnk.core.engine.dispatcher;

import io.crnk.core.engine.document.Document;

import java.util.Objects;

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
		if (!(obj instanceof Response))
			return false;
		Response other = (Response) obj;
		return Objects.equals(document, other.document) && Objects.equals(httpStatus, other.httpStatus);
	}
}
