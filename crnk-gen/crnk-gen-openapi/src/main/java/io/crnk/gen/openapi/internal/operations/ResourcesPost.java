package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.schemas.PostResource;
import io.crnk.gen.openapi.internal.schemas.ResourceResponseSchema;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class ResourcesPost extends AbstractResourceOperation implements OASOperation {

  public static final OperationType operationType = OperationType.POST;

  public ResourcesPost(MetaResource metaResource) {
    super(metaResource);
  }

  @Override
  public OperationType operationType() {
    return operationType;
  }

  @Override
  public boolean isEnabled() {
    return metaResource.isInsertable();
  }

  @Override
  public String getDescription() {
    return "Create a " + metaResource.getName();
  }

  @Override
  public Operation operation() {
    Operation operation = super.operation();
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType(
                        "application/vnd.api+json",
                        new MediaType()
                            .schema(new PostResource(metaResource).$ref()))));
    responses.put("201", new ApiResponse()
        .description("Created")
        .content(new Content()
            .addMediaType("application/vnd.api+json",
                // TODO: Adjust response based on metaResource.getRepository().isBulk()
                new MediaType().schema(new ResourceResponseSchema(metaResource).$ref()))));
    return operation.responses(apiResponsesFromMap(responses));
  }

  @Override
  public String path() {
    return OASUtils.getResourcesPath(metaResource);
  }
}
