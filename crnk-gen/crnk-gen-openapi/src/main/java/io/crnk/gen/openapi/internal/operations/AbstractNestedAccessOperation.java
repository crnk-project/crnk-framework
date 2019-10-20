package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.parameters.Fields;
import io.crnk.gen.openapi.internal.parameters.Include;
import io.crnk.gen.openapi.internal.parameters.PrimaryKey;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;

abstract class AbstractNestedAccessOperation extends AbstractNestedOperation {

  public static final OperationType operationType = OperationType.GET;

  final Map<String, ApiResponse> responses;

  private final String prefix;

  AbstractNestedAccessOperation(MetaResource metaResource, MetaResourceField metaResourceField, MetaResource relatedMetaResource) {
    super(metaResource, metaResourceField, relatedMetaResource);
    prefix = "";
    responses = defaultResponsesMap();
  }

  Operation operation() {
    Operation operation = super.operation();
    operation.getParameters().add(new PrimaryKey(metaResource).$ref());

    // TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
    // Add filter[<>] parameters
    // Only the most basic filters are documented
    if (OASUtils.oneToMany(metaResourceField)) {
      addFilters(relatedMetaResource, operation);
    }
    // Add fields[resource] parameter
    operation.getParameters().add(new Fields(relatedMetaResource).$ref());
    // Add include parameter
    operation.getParameters().add(new Include(relatedMetaResource).$ref());

    return operation;
  }
}
