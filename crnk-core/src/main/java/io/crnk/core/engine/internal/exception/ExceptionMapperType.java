package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.internal.utils.PreconditionUtil;

import java.util.Objects;

final class ExceptionMapperType {
	private final JsonApiExceptionMapper exceptionMapper;
	private final Class<? extends Throwable> exceptionClass;

	public ExceptionMapperType(Class<? extends Throwable> exceptionClass, JsonApiExceptionMapper exceptionMapper) {
		this.exceptionMapper = exceptionMapper;
		this.exceptionClass = exceptionClass;
		PreconditionUtil.assertNotNull("exceptionClass must not be null", exceptionClass);
	}

	public Class<? extends Throwable> getExceptionClass() {
		return exceptionClass;
	}

	public JsonApiExceptionMapper getExceptionMapper() {
		return exceptionMapper;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ExceptionMapperType)) {
			return false;
		}
		ExceptionMapperType that = (ExceptionMapperType) o;
		return Objects.equals(exceptionMapper, that.exceptionMapper) &&
				Objects.equals(exceptionClass, that.exceptionClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(exceptionMapper, exceptionClass);
	}

	@Override
	public String toString() {
		return "ExceptionMapperType[" +
				"exceptionClass=" + exceptionClass.getName() +
				", exceptionMapper=" + exceptionMapper +
				']';
	}
}
