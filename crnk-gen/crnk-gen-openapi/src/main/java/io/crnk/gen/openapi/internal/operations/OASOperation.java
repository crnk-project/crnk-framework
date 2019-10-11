package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OperationType;
import io.swagger.v3.oas.models.Operation;

public interface OASOperation {

  OperationType operationType();

  boolean isEnabled();

  Operation operation();

  String path();
}
