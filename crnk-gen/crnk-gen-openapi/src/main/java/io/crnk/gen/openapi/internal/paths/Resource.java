package io.crnk.gen.openapi.internal.paths;

import io.crnk.gen.openapi.internal.OASResource;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;

public class Resource extends BasePath {
  private final OASResource oasResource;
  private final String resourceName;
  private final String resourceType;

  public Resource(OASResource oasResource) {
    this.oasResource = oasResource;
    resourceName = oasResource.resourceName;
    resourceType = oasResource.resourceType;
  }

  public Operation Get() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Retrieve a " + resourceType + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ApiResponse().$ref(resourceName + "Response"));
    operation.setResponses(apiResponsesFromMap(responses));

    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + resourceType + "PrimaryKey"));
    // Add fields[resource] parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + resourceType + "Fields"));
    // Add include parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + resourceType + "Include"));

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
                new MediaType().schema(new Schema()
                    .$ref(resourceName + "Response")))));
    operation.setResponses(apiResponsesFromMap(responses));
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + resourceType + "PrimaryKey"));
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType("application/vnd.api+json",
                        new MediaType()
                            .schema(
                                new Schema()
                                    .$ref(resourceName + "Patch")))));
    return operation;
  }

  public Operation Delete() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Delete a " + resourceName);
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ApiResponse().description("OK"));
    operation.setResponses(apiResponsesFromMap(responses));
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + resourceType + "PrimaryKey"));

    return operation;
  }
}
