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
import io.swagger.v3.oas.models.media.*;
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
    openApi.getComponents().schemas(generateStandardSchemas());
    openApi.getComponents().addParameters(PageLimit.class.getSimpleName(), PageLimit.parameter());
    openApi.getComponents().addParameters(PageOffset.class.getSimpleName(), PageOffset.parameter());
    boolean NumberSizePagingBehavior = false;
    if (NumberSizePagingBehavior) {  // TODO: Figure out how to determine this
      openApi.getComponents().addParameters(PageSize.class.getSimpleName(), PageNumber.parameter());
      openApi.getComponents().addParameters(PageNumber.class.getSimpleName(), PageSize.parameter());
    }
    openApi.getComponents().addParameters(ContentType.class.getSimpleName(), ContentType.parameter());
    openApi.getComponents().addParameters(Filter.class.getSimpleName(), Filter.parameter());
    openApi.getComponents().responses(generateStandardApiResponses());
    registerMetaResources();
  }

  public OpenAPI getOpenApi() {
    return openApi;
  }

  public OpenAPI registerMetaResources() {
    // TODO: Respect @JsonApiExposed(false)
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
      PathItem resourcePathItem = openApi.getPaths().getOrDefault(oasResource.getResourcePath(), new PathItem());
      Operation operation;

      // List Response
      operation = oasResource.generateGetResourcesOperation();
      if (operation != null) {
        resourcesPathItem.setGet(mergeOperations(operation, resourcesPathItem.getGet()));
        openApi.getPaths().addPathItem(oasResource.getResourcesPath(), resourcesPathItem);
      }

      // Single Response
      operation = oasResource.generateGetResourceOperation();
      if (operation != null) {
        resourcePathItem.setGet(mergeOperations(operation, resourcePathItem.getGet()));
        openApi.getPaths().addPathItem(oasResource.getResourcePath(), resourcePathItem);
      }
      // TODO: Add Support for Bulk Operations

      // List Response
      operation = oasResource.generatePostResourcesOperation();
      if (operation != null) {
        resourcesPathItem.setPost(mergeOperations(operation, resourcesPathItem.getPost()));
        openApi.getPaths().addPathItem(oasResource.getResourcesPath(), resourcesPathItem);
      }

      // TODO: Add Support for Bulk Operations
      // Single Response
      operation = oasResource.generatePatchResourceOperation();
      if (operation != null) {
        resourcePathItem.setPatch(mergeOperations(operation, resourcePathItem.getPatch()));
        openApi.getPaths().addPathItem(oasResource.getResourcePath(), resourcePathItem);

      }

      // TODO: Add Support for Bulk Operations
      // Single Response
      operation = oasResource.generateDeleteResourceOperation();
      if (operation != null) {
        resourcePathItem.setDelete(mergeOperations(operation, resourcePathItem.getDelete()));
        openApi.getPaths().addPathItem(oasResource.getResourcePath(), resourcePathItem);
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
            PathItem relationshipPathItem = openApi.getPaths().getOrDefault(oasResource.getResourcePath() + "/relationships" + relatedOasResource.getResourcesPath(), new PathItem());


            // Add <field>/ path GET
            fieldPathItem.setGet(mergeOperations(oasResource.generateGetFieldOperation(relatedMetaResource, mrf), fieldPathItem.getGet()));
            openApi.getPaths().addPathItem(oasResource.getResourcePath() + relatedOasResource.getResourcesPath(), fieldPathItem);

            // Add relationships/ path GET
            relationshipPathItem.setGet(mergeOperations(oasResource.generateGetRelationshipsOperation(relatedMetaResource, mrf), relationshipPathItem.getGet()));
            openApi.getPaths().addPathItem(oasResource.getResourcePath() + "/relationships" + relatedOasResource.getResourcesPath(), relationshipPathItem);

            // Add <field>/ path POST
            fieldPathItem.setPost(mergeOperations(oasResource.generatePostFieldOperation(relatedMetaResource, mrf), fieldPathItem.getPost()));
            openApi.getPaths().addPathItem(oasResource.getResourcePath() + relatedOasResource.getResourcesPath(), fieldPathItem);

            // Add relationships/ path POST
            relationshipPathItem.setPost(mergeOperations(oasResource.generatePostRelationshipsOperation(relatedMetaResource, mrf), relationshipPathItem.getPost()));
            openApi.getPaths().addPathItem(oasResource.getResourcePath() + "/relationships" + relatedOasResource.getResourcesPath(), relationshipPathItem);

            // Add <field>/ path PATCH
            fieldPathItem.setPatch(mergeOperations(oasResource.generatePatchFieldOperation(relatedMetaResource, mrf), fieldPathItem.getPatch()));
            openApi.getPaths().addPathItem(oasResource.getResourcePath() + relatedOasResource.getResourcesPath(), fieldPathItem);

            // Add relationships/ path PATCH
            relationshipPathItem.setPatch(mergeOperations(oasResource.generatePatchRelationshipsOperation(relatedMetaResource, mrf), relationshipPathItem.getPatch()));
            openApi.getPaths().addPathItem(oasResource.getResourcePath() + "/relationships" + relatedOasResource.getResourcesPath(), relationshipPathItem);

            // If the relationship is updatable then we imply that it is deletable.

            // TODO: OpenAPI does not allow DELETE methods to define a RequestBody (https://github.com/OAI/OpenAPI-Specification/issues/1801)
            // Add <field>/ path DELETE
            fieldPathItem.setDelete(mergeOperations(oasResource.generateDeleteFieldOperation(relatedMetaResource, mrf), fieldPathItem.getDelete()));
            openApi.getPaths().addPathItem(oasResource.getResourcePath() + relatedOasResource.getResourcesPath(), fieldPathItem);

            // Add relationships/ path DELETE
            relationshipPathItem.setDelete(mergeOperations(oasResource.generateDeleteRelationshipsOperation(relatedMetaResource, mrf), relationshipPathItem.getDelete()));
            openApi.getPaths().addPathItem(oasResource.getResourcePath() + "/relationships" + relatedOasResource.getResourcesPath(), relationshipPathItem);
          }
        }
      }
    }
    return getOpenApi();
  }

  private Operation mergeOperations(Operation newOperation, Operation existingOperation) {
    if (existingOperation == null) {
      return newOperation;
    }

    if (existingOperation.getOperationId() != null) {
      newOperation.setOperationId(existingOperation.getOperationId());
    }

    if (existingOperation.getSummary() != null) {
      newOperation.setSummary(existingOperation.getSummary());
    }

    if (existingOperation.getDescription() != null) {
      newOperation.setDescription(existingOperation.getDescription());
    }

    if (existingOperation.getExtensions() != null) {
      newOperation.setExtensions(existingOperation.getExtensions());
    }

    return newOperation;
  }

  /*
    Generate default schemas that are common across the api.
    For example, in JSON:API, the error response is common across all APIs
   */
  private Map<String, Schema> generateStandardSchemas() {
    Map<String, Schema> schemas = new LinkedHashMap<>();

    // Standard Error Schema
    schemas.put(ApiError.class.getSimpleName(), ApiError.schema());

    // Standard wrapper responses for single & multiple records
    schemas.put(ResponseMixin.class.getSimpleName(), ResponseMixin.schema());
    schemas.put(ListResponseMixin.class.getSimpleName(), ListResponseMixin.schema());

    return schemas;
  }

  // RESPONSES

  private Map<String, ApiResponse> generateStandardApiResponses() {
    return OASUtils.mergeApiResponses(generateStandardApiSuccessResponses(), OASErrors.generateStandardApiErrorResponses());
  }

  private Map<String, ApiResponse> generateStandardApiSuccessResponses() {
    Map<String, ApiResponse> responses = new LinkedHashMap<>();
    responses.put("202", Accepted.response());
    responses.put("204", NoContent.response());

    return responses;
  }


}
