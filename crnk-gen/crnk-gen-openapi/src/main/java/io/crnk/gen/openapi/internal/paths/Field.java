package io.crnk.gen.openapi.internal.paths;


import io.crnk.gen.openapi.internal.OASResource;
import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;

public class Field extends BasePath {
  private final OASResource oasResource;
  private final OASResource relatedOasResource;
  private final MetaResourceField metaResourceField;
  private final String resourceName;
  private final String resourceType;

  public Field(OASResource oasResource, OASResource relatedOasResource, MetaResourceField metaResourceField) {
    super.oasResource = oasResource;
    this.oasResource = oasResource;
    this.relatedOasResource = relatedOasResource;
    this.metaResourceField = metaResourceField;
    resourceName = oasResource.resourceName;
    resourceType = oasResource.resourceType;
  }

  public Operation Get() {
    Operation operation = generateDefaultGetRelationshipsOrFieldsOperation(relatedOasResource, OASUtils.oneToMany(metaResourceField));
    operation.setDescription("Retrieve " + relatedOasResource.getResourceType() + " related to a " + resourceType + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = OASUtils.oneToMany(metaResourceField) ? "ListResponse" : "Response";
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix));
    operation.setResponses(apiResponsesFromMap(responses));

    return operation;
  }

  public Operation Post() {
    Operation operation = generateDefaultRelationshipOperation(relatedOasResource, OASUtils.oneToMany(metaResourceField), true);
    operation.setDescription("Create " + resourceType + " relationship to a " + relatedOasResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = OASUtils.oneToMany(metaResourceField) ? "Relationships" : "Relationship";
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix + "Response"));
    operation.setResponses(apiResponsesFromMap(responses));

    return operation;
  }

  public Operation Patch() {
    Operation operation = generateDefaultRelationshipOperation(relatedOasResource, OASUtils.oneToMany(metaResourceField), true);
    operation.setDescription("Update " + resourceType + " relationship to a " + relatedOasResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = OASUtils.oneToMany(metaResourceField) ? "Relationships" : "Relationship";
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix + "Response"));
    operation.setResponses(apiResponsesFromMap(responses));

    return operation;
  }

  public Operation Delete() {
    Operation operation = generateDefaultRelationshipOperation(relatedOasResource, OASUtils.oneToMany(metaResourceField), false);
    operation.setDescription("Delete " + resourceType + " relationship to a " + relatedOasResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = OASUtils.oneToMany(metaResourceField) ? "Relationships" : "Relationship";
    // TODO: OpenAPI does not allow DELETE methods to define a RequestBody (https://github.com/OAI/OpenAPI-Specification/issues/1801)
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix + "Response"));
    operation.setResponses(apiResponsesFromMap(responses));

    return operation;
  }
}
