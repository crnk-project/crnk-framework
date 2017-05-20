package io.crnk.rs.resource.exception.mapper;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.legacy.queryParams.errorhandling.ExceptionMapperProvider;
import io.crnk.rs.resource.exception.ExampleException;

@ExceptionMapperProvider
public class ExampleExceptionMapper implements JsonApiExceptionMapper<ExampleException> {
	@Override
	public ErrorResponse toErrorResponse(ExampleException exception) {
		return ErrorResponse.builder()
				.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500)
				.setSingleErrorData(ErrorData.builder()
						.setTitle(exception.getTitle())
						.setId(exception.getId())
						.build())
				.build();
	}
}
