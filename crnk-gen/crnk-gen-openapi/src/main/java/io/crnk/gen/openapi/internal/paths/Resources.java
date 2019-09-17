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

public class Resources extends BasePath {
  private final OASResource oasResource;
  private final String resourceName;
  private final String resourceType;

  public Resources(OASResource oasResource) {
    this.oasResource = oasResource;
    resourceName = oasResource.resourceName;
    resourceType = oasResource.resourceType;
  }

  public Operation Get() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Retrieve a List of " + resourceType + " resources");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ApiResponse().$ref(resourceName + "ListResponse"));
    operation.setResponses(apiResponsesFromMap(responses));

    // Add filters for resource
    oasResource.addFilters(operation);

    // Add fields[resource] parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + resourceType + "Fields"));

    // Add include parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + resourceType + "Include"));

    // Add sort parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + resourceType + "Sort"));

    // Add page[limit] parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/PageLimit"));

    // Add page[offset] parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/PageOffset"));

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
                new MediaType().schema(new Schema()
                    .$ref(resourceName + "Response")))));
    operation.setResponses(apiResponsesFromMap(responses));
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType(
                        "application/json",
                        new MediaType()
                            .schema(
                                new Schema()
                                    .$ref(resourceName + "Post")))));
    return operation;
  }
}
