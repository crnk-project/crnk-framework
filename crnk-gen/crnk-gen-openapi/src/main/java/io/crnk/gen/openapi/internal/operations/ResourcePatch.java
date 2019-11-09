package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.parameters.PrimaryKey;
import io.crnk.gen.openapi.internal.schemas.Info;
import io.crnk.gen.openapi.internal.schemas.PatchResource;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class ResourcePatch extends AbstractResourceOperation implements OASOperation {

  public static final OperationType operationType = OperationType.PATCH;

  public ResourcePatch(MetaResource metaResource) {
    super(metaResource);
  }

  @Override
  public OperationType operationType() {
    return operationType;
  }

  @Override
  public boolean isEnabled() {
    return metaResource.isUpdatable();
  }

  @Override
  public String getDescription() {
    return "Update a " + metaResource.getName();
  }

  @Override
  public Operation operation() {
    Operation operation = super.operation();
    operation.addParametersItem(new PrimaryKey(metaResource).$ref());
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType("application/vnd.api+json",
                        new MediaType()
                            .schema(new PatchResource(metaResource).$ref()))));
    responses.put("200", new ApiResponse()
        .description("OK")
        .content(new Content()
            .addMediaType("application/vnd.api+json",
                new MediaType().schema(new Info().$ref()))));
    return operation.responses(apiResponsesFromMap(responses));
  }

  @Override
  public String path() {
    return OASUtils.getResourcePath(metaResource);
  }
}
