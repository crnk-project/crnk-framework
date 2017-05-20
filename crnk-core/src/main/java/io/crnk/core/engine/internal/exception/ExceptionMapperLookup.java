package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.error.JsonApiExceptionMapper;

import java.util.Set;

public interface ExceptionMapperLookup {

	Set<JsonApiExceptionMapper> getExceptionMappers();
}
