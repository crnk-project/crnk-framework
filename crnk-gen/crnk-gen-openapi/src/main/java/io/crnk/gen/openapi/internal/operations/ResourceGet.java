package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.parameters.Fields;
import io.crnk.gen.openapi.internal.parameters.Include;
import io.crnk.gen.openapi.internal.parameters.PrimaryKey;
import io.crnk.gen.openapi.internal.responses.ResourceResponse;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.Operation;

public class ResourceGet extends AbstractResourceOperation implements OASOperation {

  public static final OperationType operationType = OperationType.GET;

  public ResourceGet(MetaResource metaResource) {
    super(metaResource);
  }

  @Override
  public OperationType operationType() {
    return operationType;
  }

  @Override
  public boolean isEnabled() {
    return metaResource.isReadable();
  }

  @Override
  public String getDescription() {
    return "Retrieve a " + metaResource.getResourceType() + " resource";
  }

  @Override
  public Operation operation() {
    Operation operation = super.operation();
    operation.addParametersItem(new PrimaryKey(metaResource).$ref());
    // Add fields[resource] parameter
    operation.addParametersItem(new Fields(metaResource).$ref());
    // Add include parameter
    operation.addParametersItem(new Include(metaResource).$ref());
    responses.put("200", new ResourceResponse(metaResource).$ref());
    return operation.responses(apiResponsesFromMap(responses));
  }

  @Override
  public String path() {
    return OASUtils.getResourcePath(metaResource);
  }
}
