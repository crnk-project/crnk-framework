package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.error.ExceptionMapper;

import java.util.Set;

public interface ExceptionMapperLookup {

	Set<ExceptionMapper> getExceptionMappers();
}
