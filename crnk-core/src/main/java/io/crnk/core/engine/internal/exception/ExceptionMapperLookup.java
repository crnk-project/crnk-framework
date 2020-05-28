package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.error.ExceptionMapper;

import java.util.List;

public interface ExceptionMapperLookup {

    List<ExceptionMapper> getExceptionMappers();
}
