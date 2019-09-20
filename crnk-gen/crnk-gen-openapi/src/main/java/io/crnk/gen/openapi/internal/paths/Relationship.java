package io.crnk.gen.openapi.internal.paths;

import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.gen.openapi.internal.responses.ResourceReferenceResponse;
import io.crnk.gen.openapi.internal.responses.ResourceReferencesResponse;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;

public class Relationship extends AbstractFieldPath {
  private final MetaResource metaResource;
  private final MetaResource relatedMetaResource;
  private final MetaResourceField metaResourceField;
  private final String resourceName;
  private final String resourceType;

  public Relationship(MetaResource metaResource, MetaResource relatedMetaResource, MetaResourceField metaResourceField) {
    super.metaResource = metaResource;
    this.metaResource = metaResource;
    this.relatedMetaResource = relatedMetaResource;
    this.metaResourceField = metaResourceField;
    resourceName = metaResource.getName();
    resourceType = metaResource.getResourceType();
  }

  public Operation Get() {
    Operation operation = generateDefaultGetRelationshipsOrFieldsOperation(relatedMetaResource, OASUtils.oneToMany(metaResourceField));
    operation.setDescription("Retrieve " + relatedMetaResource.getResourceType() + " references related to a " + resourceType + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    ApiResponse responseSchema = OASUtils.oneToMany(metaResourceField) ? new ResourceReferencesResponse(relatedMetaResource).$ref() : new ResourceReferenceResponse(relatedMetaResource).$ref();
    responses.put("200", responseSchema);
    operation.setResponses(apiResponsesFromMap(responses));

    return operation;
  }

  public Operation Post() {
    Operation operation = generateDefaultRelationshipOperation(relatedMetaResource, OASUtils.oneToMany(metaResourceField), true);
    operation.setDescription("Create " + resourceType + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    ApiResponse responseSchema = OASUtils.oneToMany(metaResourceField) ? new ResourceReferencesResponse(relatedMetaResource).$ref() : new ResourceReferenceResponse(relatedMetaResource).$ref();
    responses.put("200", responseSchema);
    operation.setResponses(apiResponsesFromMap(responses));

    return operation;
  }

  public Operation Patch() {
    Operation operation = generateDefaultRelationshipOperation(relatedMetaResource, OASUtils.oneToMany(metaResourceField), true);
    operation.setDescription("Update " + resourceType + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    ApiResponse responseSchema = OASUtils.oneToMany(metaResourceField) ? new ResourceReferencesResponse(relatedMetaResource).$ref() : new ResourceReferenceResponse(relatedMetaResource).$ref();
    responses.put("200", responseSchema);
    operation.setResponses(apiResponsesFromMap(responses));

    return operation;
  }

  public Operation Delete() {
    Operation operation = generateDefaultRelationshipOperation(relatedMetaResource, OASUtils.oneToMany(metaResourceField), false);
    operation.setDescription("Delete " + resourceType + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    ApiResponse responseSchema = OASUtils.oneToMany(metaResourceField) ? new ResourceReferencesResponse(relatedMetaResource).$ref() : new ResourceReferenceResponse(relatedMetaResource).$ref();
    responses.put("200", responseSchema);
    operation.setResponses(apiResponsesFromMap(responses));

    return operation;
  }
}
