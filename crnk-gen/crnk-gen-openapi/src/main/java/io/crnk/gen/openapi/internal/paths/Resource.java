package io.crnk.gen.openapi.internal.paths;

import io.crnk.gen.openapi.internal.parameters.Fields;
import io.crnk.gen.openapi.internal.parameters.Include;
import io.crnk.gen.openapi.internal.parameters.PrimaryKey;
import io.crnk.gen.openapi.internal.responses.ResourceResponse;
import io.crnk.gen.openapi.internal.schemas.PatchResource;
import io.crnk.gen.openapi.internal.schemas.ResourceResponseSchema;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;

public class Resource extends AbstractResourcePath {
  private final String resourceName;
  private final String resourceType;

  public Resource(MetaResource metaResource) {
    super.metaResource = metaResource;
    resourceName = metaResource.getName();
    resourceType = metaResource.getResourceType();
  }

  public Operation Get() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Retrieve a " + resourceType + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ResourceResponse(metaResource).$ref());
    operation.setResponses(apiResponsesFromMap(responses));

    operation.getParameters().add(new PrimaryKey(metaResource).$ref());
    // Add fields[resource] parameter
    operation.getParameters().add(new Fields(metaResource).$ref());
    // Add include parameter
    operation.getParameters().add(new Include(metaResource).$ref());

    return operation;
  }

  public Operation Patch() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Update a " + resourceName);
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ApiResponse()
        .description("OK")
        .content(new Content()
            .addMediaType("application/vnd.api+json",
                new MediaType().schema(new ResourceResponseSchema(metaResource).$ref()))));
    operation.setResponses(apiResponsesFromMap(responses));
    operation.getParameters().add(new PrimaryKey(metaResource).$ref());
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType("application/vnd.api+json",
                        new MediaType()
                            .schema(new PatchResource(metaResource).$ref()))));
    return operation;
  }

  public Operation Delete() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Delete a " + resourceName);
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ApiResponse().description("OK"));
    operation.setResponses(apiResponsesFromMap(responses));
    operation.getParameters().add(new PrimaryKey(metaResource).$ref());

    return operation;
  }
}
