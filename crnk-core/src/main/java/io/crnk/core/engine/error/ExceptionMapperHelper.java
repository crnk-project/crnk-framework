package io.crnk.core.engine.error;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;

public class ExceptionMapperHelper {

	private ExceptionMapperHelper(){
	}

	private static final String META_TYPE_KEY = "type";

	public static ErrorResponse toErrorResponse(Throwable exception, int statusCode, String metaTypeValue) {
		List<ErrorData> errors = new ArrayList<>();

		ErrorDataBuilder builder = ErrorData.builder();
		builder = builder.addMetaField(ExceptionMapperHelper.META_TYPE_KEY, metaTypeValue);
		builder = builder.setStatus(String.valueOf(statusCode));
		builder = builder.setCode(exception.getMessage());
		builder = builder.setTitle(exception.getLocalizedMessage());

		ErrorData error = builder.build();
		errors.add(error);

		return ErrorResponse.builder().setStatus(statusCode).setErrorData(errors).build();
	}

	public static String createErrorMessage(ErrorResponse errorResponse) {
		Iterator<ErrorData> errors = errorResponse.getErrors().iterator();
		String message = null;
		if (errors.hasNext()) {
			ErrorData data = errors.next();
			message = data.getCode();
		}
		return message;
	}

	public static boolean accepts(ErrorResponse errorResponse, int acceptedStatusCode, String metaTypeValue) {
		if (errorResponse.getHttpStatus() != acceptedStatusCode) {
			return false;
		}
		Iterator<ErrorData> errors = errorResponse.getErrors().iterator();
		if (!errors.hasNext()) {
			return false;
		}
		ErrorData error = errors.next();
		Map<String, Object> meta = error.getMeta();
		return meta != null && metaTypeValue.equals(meta.get(META_TYPE_KEY));
	}

}
