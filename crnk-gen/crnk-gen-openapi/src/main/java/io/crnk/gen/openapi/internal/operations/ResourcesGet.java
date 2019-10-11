package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASResource;
import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.parameters.Fields;
import io.crnk.gen.openapi.internal.parameters.Include;
import io.crnk.gen.openapi.internal.parameters.PageLimit;
import io.crnk.gen.openapi.internal.parameters.PageOffset;
import io.crnk.gen.openapi.internal.parameters.Sort;
import io.crnk.gen.openapi.internal.responses.ResourcesResponse;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.Operation;

public class ResourcesGet extends AbstractResourceOperation implements OASOperation {

  public static final OperationType operationType = OperationType.GET;

  public ResourcesGet(MetaResource metaResource) {
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
    return "Retrieve a List of " + metaResource.getResourceType() + " resources";
  }

  @Override
  public Operation operation() {
    Operation operation = super.operation();
    // Add filters for resource
    OASResource.addFilters(metaResource, operation);

    // Add fields[resource] parameter
    operation.addParametersItem(new Fields(metaResource).$ref());

    // Add include parameter
    operation.addParametersItem(new Include(metaResource).$ref());

    // Add sort parameter
    operation.addParametersItem(new Sort(metaResource).$ref());

    // Add page[limit] parameter
    operation.addParametersItem(new PageLimit().$ref());

    // Add page[offset] parameter
    operation.addParametersItem(new PageOffset().$ref());

    responses.put("200", new ResourcesResponse(metaResource).$ref());
    return operation.responses(apiResponsesFromMap(responses));
  }

  @Override
  public String path() {
    return OASUtils.getResourcesPath(metaResource);
  }
}
