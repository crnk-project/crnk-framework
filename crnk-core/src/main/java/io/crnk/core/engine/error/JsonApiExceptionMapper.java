package io.crnk.core.engine.error;

/**
 * @deprecated use {@link ExceptionMapper} instead which supports crnk-client as well.
 */
@Deprecated
public interface JsonApiExceptionMapper<E extends Throwable> {

	ErrorResponse toErrorResponse(E exception);
}
