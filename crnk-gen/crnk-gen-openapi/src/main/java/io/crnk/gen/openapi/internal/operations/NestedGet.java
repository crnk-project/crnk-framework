package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.responses.ResourceResponse;
import io.crnk.gen.openapi.internal.responses.ResourcesResponse;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class NestedGet extends AbstractNestedAccessOperation implements OASOperation {

  public NestedGet(MetaResource metaResource, MetaResourceField metaResourceField, MetaResource relatedMetaResource) {
    super(metaResource, metaResourceField, relatedMetaResource);
  }

  @Override
  public OperationType operationType() {
    return operationType;
  }

  @Override
  public boolean isEnabled() {
    return metaResource.isReadable() && metaResourceField.isReadable();
  }

  @Override
  public String getDescription() {
    return "Retrieve " + relatedMetaResource.getResourceType() + " related to a " + metaResource.getResourceType() + " resource";
  }

  @Override
  public Operation operation() {
    Operation operation = super.operation();
    ApiResponse responseSchema = OASUtils.oneToMany(metaResourceField) ? new ResourcesResponse(relatedMetaResource).$ref() : new ResourceResponse(relatedMetaResource).$ref();
    responses.put("200", responseSchema);
    return operation.responses(apiResponsesFromMap(responses));
  }

  @Override
  public String path() {
    return OASUtils.getNestedPath(metaResource, relatedMetaResource);
  }
}
