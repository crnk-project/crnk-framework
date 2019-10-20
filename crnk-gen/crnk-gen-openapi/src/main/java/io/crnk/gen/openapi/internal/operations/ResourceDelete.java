package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.parameters.PrimaryKey;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class ResourceDelete extends AbstractResourceOperation implements OASOperation {

  public static final OperationType operationType = OperationType.DELETE;

  public ResourceDelete(MetaResource metaResource) {
    super(metaResource);
  }

  @Override
  public OperationType operationType() {
    return operationType;
  }

  @Override
  public boolean isEnabled() {
    return metaResource.isDeletable();
  }

  @Override
  public String getDescription() {
    return "Delete a " + metaResource.getName();
  }

  @Override
  public Operation operation() {
    Operation operation = super.operation();
    operation.addParametersItem(new PrimaryKey(metaResource).$ref());
    responses.put("200", new ApiResponse().description("OK"));
    return operation.responses(apiResponsesFromMap(responses));
  }

  @Override
  public String path() {
    return OASUtils.getResourcePath(metaResource);
  }
}
