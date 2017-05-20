package io.crnk.test.mock;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.repository.response.JsonApiResponse;

import java.util.List;

public class TestExceptionMapper implements ExceptionMapper<TestException> {

	@Override
	public ErrorResponse toErrorResponse(TestException cve) {
		ErrorData error = ErrorData.builder().setDetail(cve.getMessage()).build();
		return ErrorResponse.builder().setStatus(499).setSingleErrorData(error).build();
	}

	@Override
	public TestException fromErrorResponse(ErrorResponse errorResponse) {
		JsonApiResponse response = errorResponse.getResponse();
		List<ErrorData> errors = (List<ErrorData>) response.getEntity();
		StringBuilder message = new StringBuilder();
		for (ErrorData error : errors) {
			String title = error.getDetail();
			message.append(title);
		}
		return new TestException(message.toString());
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return errorResponse.getHttpStatus() == 499;
	}

	// @Override
	// public ErrorResponse toErrorResponse(ConstraintViolationException cve) {
	// List<ErrorData> errors = new ArrayList<>();
	// for (ConstraintViolation<?> violation : cve.getConstraintViolations()) {
	// ErrorData error =
	// ErrorData.builder().setStatus(String.valueOf(HttpStatus.UNPROCESSABLE_422))
	// .setTitle(violation.getMessage()).setSourcePointer(createSourcePointer(violation)).build();
	//
	// errors.add(error);
	// }
	//
	// return
	// ErrorResponse.builder().setStatus(HttpStatus.UNPROCESSABLE_422).setErrorData(errors).build();
	// }
	//
	// private String createSourcePointer(ConstraintViolation<?> violation) {
	// String attributeName = findAttributeName(violation);
	// String sourcePointer = "/data/attributes/" + attributeName;
	// return sourcePointer;
	// }
	//
	// private String findAttributeName(ConstraintViolation<?> violation) {
	// for (Iterator<Node> iterator = violation.getPropertyPath().iterator();
	// iterator.hasNext();) {
	// Node node = iterator.next();
	// if (!iterator.hasNext()) {
	// return node.getName();
	// }
	// }
	// return null;
	// }

}