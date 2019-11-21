package io.crnk.gen.openapi.internal.operations;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.OperationType;
import io.crnk.gen.openapi.internal.schemas.PostResource;
import io.crnk.gen.openapi.internal.schemas.PostResources;
import io.crnk.gen.openapi.internal.schemas.ResourceResponseSchema;
import io.crnk.gen.openapi.internal.schemas.ResourcesResponseSchema;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Arrays;

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
    if (metaResource.getRepository().isBulk()) {
      return "Create one (or more) " + metaResource.getName();
    }
    return "Create a " + metaResource.getName();
  }

  @Override
  public Operation operation() {
    Operation operation = super.operation();

    Schema requestSchema = new PostResource(metaResource).$ref();
    Schema responseSchema = new ResourceResponseSchema(metaResource).$ref();

    if (metaResource.getRepository().isBulk()) {
      requestSchema = new ComposedSchema()
          .oneOf(
              Arrays.asList(
                  new PostResource(metaResource).$ref(),
                  new PostResources(metaResource).$ref()));
      responseSchema = new ComposedSchema()
          .oneOf(
              Arrays.asList(
                  new ResourceResponseSchema(metaResource).$ref(),
                  new ResourcesResponseSchema(metaResource).$ref()));
    }
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType(
                        "application/vnd.api+json",
                        new MediaType()
                            .schema(requestSchema))));
    responses.put("201", new ApiResponse()
        .description("Created")
        .content(new Content()
            .addMediaType("application/vnd.api+json",
                new MediaType().schema(responseSchema))));
    return operation.responses(apiResponsesFromMap(responses));
  }

  @Override
  public String path() {
    return OASUtils.getResourcesPath(metaResource);
  }
}
