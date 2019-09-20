package io.crnk.gen.openapi.internal;

import io.crnk.gen.openapi.internal.parameters.*;
import io.crnk.gen.openapi.internal.responses.Accepted;
import io.crnk.gen.openapi.internal.responses.NoContent;
import io.crnk.gen.openapi.internal.schemas.ApiError;
import io.crnk.gen.openapi.internal.schemas.ListResponseMixin;
import io.crnk.gen.openapi.internal.schemas.ResponseMixin;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OASGenerator {
  private static final Logger LOGGER = LoggerFactory.getLogger(OASGenerator.class);
  private OpenAPI openApi;
  private MetaLookup meta;
  private Map<String, OASResource> oasResources = new HashMap<>();

  public OASGenerator(MetaLookup metaLookup, OpenAPI baseOpenAPI) {
    openApi = baseOpenAPI;
    meta = metaLookup;
    openApi.getComponents().addSchemas(ApiError.getName(), ApiError.schema());
    openApi.getComponents().addSchemas(ResponseMixin.getName(), ResponseMixin.schema());
    openApi.getComponents().addSchemas(ListResponseMixin.getName(), ListResponseMixin.schema());
    openApi.getComponents().addParameters(new PageLimit().getName(), PageLimit.parameter());
    openApi.getComponents().addParameters(new PageOffset().getName(), PageOffset.parameter());
    boolean NumberSizePagingBehavior = false;
    if (NumberSizePagingBehavior) {  // TODO: Figure out how to determine this
      openApi.getComponents().addParameters(new PageSize().getName(), PageNumber.parameter());
      openApi.getComponents().addParameters(new PageNumber().getName(), PageSize.parameter());
    }
//    openApi.getComponents().addParameters(new ContentType().getName(), ContentType.parameter());
    openApi.getComponents().addParameters(new Filter().getName(), Filter.parameter());
    openApi.getComponents().responses(generateStandardApiResponses());
    registerMetaResources();
  }

  public OpenAPI getOpenApi() {
    return openApi;
  }

  public OpenAPI registerMetaResources() {
    List<MetaResource> metaResources = getJsonApiResources(meta);
    metaResources.stream().map(OASResource::new).forEach(this::register);
    return getOpenApi();
  }

  public OpenAPI register(OASResource oasResource) {
    oasResource.getComponentParameters().forEach(openApi.getComponents()::addParameters);
    oasResource.getComponentSchemas().forEach(openApi.getComponents()::addSchemas);
    oasResource.getComponentResponses().forEach(openApi.getComponents()::addResponses);
    oasResources.put(oasResource.getResourceName(), oasResource);
    return getOpenApi();
  }

  private List<MetaResource> getJsonApiResources(MetaLookup metaLookup) {
    List<MetaResource> list = new ArrayList<>();
    for (MetaResource it : metaLookup.findElements(MetaResource.class)) {
      if (isJsonApiResource(it)) {
        list.add(it);
      }
    }
    list.sort(Comparator.comparing(MetaResource::getResourceType));
    return list;
  }

  private boolean isJsonApiResource(MetaResource metaResource) {
    return metaResource.getSuperType() == null
        && !metaResource.getResourceType().startsWith("meta/")
        && metaResource.getRepository() != null
        && metaResource.getRepository().isExposed();
  }


  public OpenAPI buildPaths() {

    for (OASResource oasResource : oasResources.values()) {

      PathItem resourcesPathItem = openApi.getPaths().getOrDefault(oasResource.getResourcesPath(), new PathItem());
      for (Map.Entry<OperationType, Operation> entry : oasResource.generateResourcesOperations().entrySet()) {
        openApi.getPaths().addPathItem(oasResource.getResourcesPath(), entry.getKey().merge(resourcesPathItem, entry.getValue()));
      }

      PathItem resourcePathItem = openApi.getPaths().getOrDefault(oasResource.getResourcePath(), new PathItem());
      for (Map.Entry<OperationType, Operation> entry : oasResource.generateResourceOperations().entrySet()) {
        openApi.getPaths().addPathItem(oasResource.getResourcePath(), entry.getKey().merge(resourcePathItem, entry.getValue()));
      }

      // Relationships can be accessed in 2 ways:
      //  1.	/api/A/1/b  								The full related resource
      //  2.	/api/A/1/relationships/b		The "ids" as belong to the resource
      // Generate GET Operations for /api/A/1/B relationship path
      for (MetaElement child : oasResource.getChildren()) {
        if (child == null) {
          continue;
        } else if (child instanceof MetaPrimaryKey) {
          continue;
        } else if (((MetaResourceField) child).isPrimaryKeyAttribute()) {
          continue;
        } else if (child instanceof MetaResourceField) {
          MetaResourceField mrf = (MetaResourceField) child;
          Schema attributeSchema = OASUtils.transformMetaResourceField(mrf.getType());
          attributeSchema.nullable(mrf.isNullable());
          oasResource.getAttributes().put(mrf.getName(), attributeSchema);
          if (mrf.isAssociation()) {
            MetaResource relatedMetaResource = (MetaResource) mrf.getType().getElementType();
            OASResource relatedOasResource = oasResources.get(relatedMetaResource.getName());

            PathItem fieldPathItem = openApi.getPaths().getOrDefault(oasResource.getResourcePath() + oasResource.getResourcesPath(), new PathItem());
            for (Map.Entry<OperationType, Operation> entry : oasResource.generateFieldOperationsForField(relatedMetaResource, mrf).entrySet()) {
              openApi.getPaths().addPathItem(oasResource.getFieldPath(relatedOasResource), entry.getKey().merge(fieldPathItem, entry.getValue()));
            }

            PathItem relationshipPathItem = openApi.getPaths().getOrDefault(oasResource.getResourcePath() + "/relationships" + relatedOasResource.getResourcesPath(), new PathItem());
            for (Map.Entry<OperationType, Operation> entry : oasResource.generateRelationshipsOperationsForField(relatedMetaResource, mrf).entrySet()) {
              openApi.getPaths().addPathItem(oasResource.getRelationshipsPath(relatedOasResource), entry.getKey().merge(relationshipPathItem, entry.getValue()));
            }
          }
        }
      }
    }
    return getOpenApi();
  }

  // RESPONSES

  private Map<String, ApiResponse> generateStandardApiResponses() {
    return OASUtils.mergeApiResponses(generateStandardApiSuccessResponses(), OASErrors.generateStandardApiErrorResponses());
  }

  private Map<String, ApiResponse> generateStandardApiSuccessResponses() {
    Map<String, ApiResponse> responses = new LinkedHashMap<>();
    responses.put(new Accepted().getName(), Accepted.response());
    responses.put(new NoContent().getName(), NoContent.response());

    return responses;
  }
}