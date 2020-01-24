package io.crnk.test.mock;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.repository.response.JsonApiResponse;

import java.util.List;

public class TestExceptionMapper implements ExceptionMapper<TestException> {

	public static final int HTTP_ERROR_CODE = 499;

	@Override
	public ErrorResponse toErrorResponse(TestException cve) {
		ErrorData error = ErrorData.builder().setDetail(cve.getMessage()).build();
		return ErrorResponse.builder().setStatus(HTTP_ERROR_CODE).setSingleErrorData(error).build();
	}

	@Override
	public TestException fromErrorResponse(ErrorResponse errorResponse) {
		JsonApiResponse response = errorResponse.getResponse();
		List<ErrorData> errors = (List<ErrorData>) response.getErrors();
		StringBuilder message = new StringBuilder();
		for (ErrorData error : errors) {
			String title = error.getDetail();
			message.append(title);
		}
		return new TestException(message.toString());
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return errorResponse.getHttpStatus() == HTTP_ERROR_CODE;
	}
}
