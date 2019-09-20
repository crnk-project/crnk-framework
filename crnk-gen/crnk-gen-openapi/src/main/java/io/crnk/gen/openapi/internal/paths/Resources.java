package io.crnk.gen.openapi.internal.paths;

import io.crnk.gen.openapi.internal.OASResource;
import io.crnk.gen.openapi.internal.parameters.*;
import io.crnk.gen.openapi.internal.responses.ResourcesResponse;
import io.crnk.gen.openapi.internal.schemas.PostResource;
import io.crnk.gen.openapi.internal.schemas.ResourceResponseSchema;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;

public class Resources extends AbstractResourcePath {
  private final MetaResource metaResource;
  private final String resourceName;
  private final String resourceType;

  public Resources(MetaResource metaResource) {
    this.metaResource = metaResource;
    resourceName = metaResource.getName();
    resourceType = metaResource.getResourceType();
  }

  public Operation Get() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Retrieve a List of " + resourceType + " resources");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ResourcesResponse(metaResource).$ref());
    operation.setResponses(apiResponsesFromMap(responses));

    // Add filters for resource
    OASResource.addFilters(metaResource, operation);

    // Add fields[resource] parameter
    operation.getParameters().add(new Fields(metaResource).$ref());

    // Add include parameter
    operation.getParameters().add(new Include(metaResource).$ref());

    // Add sort parameter
    operation.getParameters().add(new Sort(metaResource).$ref());

    // Add page[limit] parameter
    operation.getParameters().add(new PageLimit().$ref());

    // Add page[offset] parameter
    operation.getParameters().add(new PageOffset().$ref());

    return operation;
  }

  public Operation Post() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Create a " + resourceName);
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("201", new ApiResponse()
        .description("Created")
        .content(new Content()
            .addMediaType("application/vnd.api+json",
                new MediaType().schema(new ResourceResponseSchema(metaResource).$ref()))));
    operation.setResponses(apiResponsesFromMap(responses));
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType(
                        "application/json",
                        new MediaType()
                            .schema(new PostResource(metaResource).$ref()))));
    return operation;
  }
}
