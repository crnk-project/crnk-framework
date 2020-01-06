package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.responses.ResourceReferenceResponse;
import io.crnk.gen.openapi.internal.responses.ResourceReferencesResponse;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class NestedPost extends AbstractNestedMutateOperation implements OASOperation {

  public static OperationType operationType = OperationType.POST;

  public NestedPost(MetaResource metaResource, MetaResourceField metaResourceField, MetaResource relatedMetaResource) {
    super(metaResource, metaResourceField, relatedMetaResource);
  }

  @Override
  public OperationType operationType() {
    return operationType;
  }

  @Override
  public boolean isEnabled() {
    return metaResource.isReadable() && metaResourceField.isInsertable();
  }

  @Override
  public String getDescription() {
    return "Create " + metaResource.getResourceType() + " relationship to a " + relatedMetaResource.getResourceType() + " resource";
  }

  @Override
  public Operation operation() {
    Operation operation = super.operation();
    ApiResponse responseSchema = OASUtils.oneToMany(metaResourceField) ? new ResourceReferencesResponse(relatedMetaResource).$ref() : new ResourceReferenceResponse(relatedMetaResource).$ref();
    responses.put("200", responseSchema);
    return operation.responses(apiResponsesFromMap(responses));
  }

  @Override
  public String path() {
    return OASUtils.getNestedPath(metaResource, metaResourceField);
  }
}
