package io.crnk.core.engine.error;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.repository.response.JsonApiResponse;

public final class ErrorResponse {

	public static final String ERRORS = "errors";

	private final Collection<ErrorData> data;

	private final int httpStatus;

	public ErrorResponse(Collection<ErrorData> data, int httpStatus) {
		this.data = data;
		this.httpStatus = httpStatus;
	}

	public static ErrorResponseBuilder builder() {
		return new ErrorResponseBuilder();
	}

	public Collection<ErrorData> getErrors() {
		if (data == null) {
			return Collections.emptyList();
		}
		return data;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public JsonApiResponse getResponse() {
		return new JsonApiResponse()
				.setEntity(data);
	}

	public JsonPath getJsonPath() {
		return null;
	}

	public QueryAdapter getQueryAdapter() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ErrorResponse)) {
			return false;
		}
		ErrorResponse that = (ErrorResponse) o;
		return Objects.equals(httpStatus, that.httpStatus) &&
				Objects.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data, httpStatus);
	}

	public Response toResponse() {
		Document responseDocument = new Document();
		List<ErrorData> errors = new ArrayList<>();
		for (ErrorData error : getErrors()) {
			errors.add(error);
		}
		responseDocument.setErrors(errors);

		return new Response(responseDocument, getHttpStatus());
	}

}