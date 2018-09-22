package io.crnk.example.springboot.domain.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CustomExceptionMapper implements ExceptionMapper<CustomException> {

	private static final int CUSTOM_ERROR_STATUS_CODE = 599;

	@Override
	public ErrorResponse toErrorResponse(CustomException e) {
		ErrorDataBuilder builder = ErrorData.builder();
		builder.setStatus(String.valueOf(CUSTOM_ERROR_STATUS_CODE));
		builder.setTitle(e.getMessage());
		ErrorData error = builder.build();
		List<ErrorData> errors = Arrays.asList(error);
		return ErrorResponse.builder().setStatus(CUSTOM_ERROR_STATUS_CODE).setErrorData(errors).build();
	}

	@Override
	public CustomException fromErrorResponse(ErrorResponse errorResponse) {
		return new CustomException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return errorResponse.getHttpStatus() == CUSTOM_ERROR_STATUS_CODE;
	}

}
