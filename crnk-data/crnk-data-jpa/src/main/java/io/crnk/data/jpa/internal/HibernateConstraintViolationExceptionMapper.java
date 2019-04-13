package io.crnk.data.jpa.internal;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;
import org.hibernate.exception.ConstraintViolationException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HibernateConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

	private static final String META_TYPE_KEY = "type";

	// assign ID do identity among different UNPROCESSABLE_ENTITY_422 results
	private static final String HIBERNATE_CONSTRAINT_VIOLATION_EXCEPTION = "HibernateConstraintViolation";

	@Override
	public ErrorResponse toErrorResponse(ConstraintViolationException cve) {
		HashMap<String, Object> meta = new HashMap<>();
		meta.put(META_TYPE_KEY, HIBERNATE_CONSTRAINT_VIOLATION_EXCEPTION);

		ErrorData error = ErrorData.builder()
				.setMeta(meta)
				.setStatus(Integer.toString(HttpStatus.UNPROCESSABLE_ENTITY_422))
				.setCode(cve.getConstraintName()).setDetail(cve.getCause() != null ? cve.getCause().getMessage() : cve.getMessage())
				.build();
		return ErrorResponse.builder().setStatus(HttpStatus.UNPROCESSABLE_ENTITY_422).setSingleErrorData(error).build();
	}

	@Override
	public ConstraintViolationException fromErrorResponse(ErrorResponse errorResponse) {
		Iterable<ErrorData> errors = errorResponse.getErrors();
		ErrorData error = errors.iterator().next();
		String msg = error.getDetail();
		String constraintName = error.getCode();
		return new ConstraintViolationException(msg, null, constraintName);
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		if (errorResponse.getHttpStatus() != HttpStatus.UNPROCESSABLE_ENTITY_422) {
			return false;
		}

		Iterable<ErrorData> errors = errorResponse.getErrors();
		Iterator<ErrorData> iterator = errors.iterator();
		if (!iterator.hasNext())
			return false;
		ErrorData errorData = iterator.next();

		Map<String, Object> meta = errorData.getMeta();
		return meta != null && HIBERNATE_CONSTRAINT_VIOLATION_EXCEPTION.equals(meta.get(META_TYPE_KEY));
	}

}
