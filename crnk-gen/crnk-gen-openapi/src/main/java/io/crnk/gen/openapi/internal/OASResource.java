package io.crnk.gen.openapi.internal;

import io.crnk.gen.openapi.internal.parameters.FieldFilter;
import io.crnk.gen.openapi.internal.parameters.Fields;
import io.crnk.gen.openapi.internal.parameters.Include;
import io.crnk.gen.openapi.internal.parameters.NestedFilter;
import io.crnk.gen.openapi.internal.parameters.PrimaryKey;
import io.crnk.gen.openapi.internal.parameters.Sort;
import io.crnk.gen.openapi.internal.paths.Field;
import io.crnk.gen.openapi.internal.paths.Relationship;
import io.crnk.gen.openapi.internal.paths.Resource;
import io.crnk.gen.openapi.internal.paths.Resources;
import io.crnk.gen.openapi.internal.responses.ResourceReferenceResponse;
import io.crnk.gen.openapi.internal.responses.ResourceReferencesResponse;
import io.crnk.gen.openapi.internal.responses.ResourceResponse;
import io.crnk.gen.openapi.internal.responses.ResourcesResponse;
import io.crnk.gen.openapi.internal.schemas.PatchResource;
import io.crnk.gen.openapi.internal.schemas.PostResource;
import io.crnk.gen.openapi.internal.schemas.ResourceAttributes;
import io.crnk.gen.openapi.internal.schemas.ResourcePatchAttributes;
import io.crnk.gen.openapi.internal.schemas.ResourcePostAttributes;
import io.crnk.gen.openapi.internal.schemas.ResourceReference;
import io.crnk.gen.openapi.internal.schemas.ResourceReferenceResponseSchema;
import io.crnk.gen.openapi.internal.schemas.ResourceReferencesResponseSchema;
import io.crnk.gen.openapi.internal.schemas.ResourceResponseSchema;
import io.crnk.gen.openapi.internal.schemas.ResourceSchema;
import io.crnk.gen.openapi.internal.schemas.ResourcesResponseSchema;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OASResource {

  private final String resourceName;

  private final String resourceType;

  private MetaResource metaResource;

  private Map<String, Schema> attributes;

  private Map<String, Parameter> componentParameters;

  private Map<String, Schema> componentSchemas;

  private Map<String, ApiResponse> componentResponses;

  OASResource(MetaResource metaResource) {
    this.metaResource = metaResource;
    resourceName = metaResource.getName();
    resourceType = metaResource.getResourceType();
    initializeAttributes();
    initializeComponentParameters();
    initializeComponentSchemas();
    initializeComponentResponses();
  }

  public static Operation addFilters(MetaResource metaResource, Operation operation) {
    // TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
    operation.getParameters().add(new NestedFilter().$ref());

    // Add filter[<>] parameters
    // Only the most basic filters are documented
    for (MetaElement child : metaResource.getChildren()) {
      if (child instanceof MetaResourceField) {
        MetaResourceField metaResourceField = (MetaResourceField) child;
        if (metaResourceField.isFilterable()) {
          if (metaResourceField.isLinks() || metaResourceField.isMeta()) {
            continue;
          }
          operation.getParameters().add(new FieldFilter(metaResourceField).parameter());
        }
      }
    }
    return operation;
  }

  private void initializeAttributes() {
    attributes = new HashMap<>();
    for (MetaElement child : metaResource.getChildren()) {
      if (child == null) {
        continue;
      }
      if (child instanceof MetaPrimaryKey) {
        continue;
      }
      if (((MetaResourceField) child).isPrimaryKeyAttribute()) {
        continue;
      }

      MetaResourceField mrf = (MetaResourceField) child;
      Schema attributeSchema = OASUtils.transformMetaResourceField(mrf.getType());
      attributeSchema.nullable(mrf.isNullable());
      attributes.put(mrf.getName(), attributeSchema);
    }
  }

  private void initializeComponentParameters() {
    componentParameters = new HashMap<>();
    componentParameters.put(new PrimaryKey(metaResource).getName(), new PrimaryKey(metaResource).parameter());
    componentParameters.put(new Fields(metaResource).getName(), new Fields(metaResource).parameter());
    componentParameters.put(new Include(metaResource).getName(), new Include(metaResource).parameter());
    componentParameters.put(new Sort(metaResource).getName(), new Sort(metaResource).parameter());
  }

  private void initializeComponentSchemas() {
    componentSchemas = new HashMap<>();
    componentSchemas.put(new ResourceReference(metaResource).getName(), new ResourceReference(metaResource).schema());
    componentSchemas.put(new ResourceAttributes(metaResource).getName(), new ResourceAttributes(metaResource).schema());
    componentSchemas.put(new ResourcePostAttributes(metaResource).getName(), new ResourcePostAttributes(metaResource).schema());
    componentSchemas.put(new ResourcePatchAttributes(metaResource).getName(), new ResourcePatchAttributes(metaResource).schema());
    componentSchemas.put(new ResourceSchema(metaResource).getName(), new ResourceSchema(metaResource).schema());
    componentSchemas.put(new PatchResource(metaResource).getName(), new PatchResource(metaResource).schema());
    componentSchemas.put(new PostResource(metaResource).getName(), new PostResource(metaResource).schema());
    componentSchemas.put(new ResourceResponseSchema(metaResource).getName(), new ResourceResponseSchema(metaResource).schema());
    componentSchemas.put(new ResourcesResponseSchema(metaResource).getName(), new ResourcesResponseSchema(metaResource).schema());
    componentSchemas.put(new ResourceReferenceResponseSchema(metaResource).getName(), new ResourceReferenceResponseSchema(metaResource).schema());
    componentSchemas.put(new ResourceReferencesResponseSchema(metaResource).getName(), new ResourceReferencesResponseSchema(metaResource).schema());
  }

  private void initializeComponentResponses() {
    componentResponses = new HashMap<>();
    componentResponses.put(new ResourceResponse(metaResource).getName(), new ResourceResponse(metaResource).response());
    componentResponses.put(new ResourcesResponse(metaResource).getName(), new ResourcesResponse(metaResource).response());
    componentResponses.put(new ResourceReferenceResponse(metaResource).getName(), new ResourceReferenceResponse(metaResource).response());
    componentResponses.put(new ResourceReferencesResponse(metaResource).getName(), new ResourceReferencesResponse(metaResource).response());
  }

  Map<String, Schema> getAttributes() {
    return attributes;
  }

  Map<String, Parameter> getComponentParameters() {
    return componentParameters;
  }

  Map<String, Schema> getComponentSchemas() {
    return componentSchemas;
  }

  Map<String, ApiResponse> getComponentResponses() {
    return componentResponses;
  }

  String getResourceName() {
    return resourceName;
  }

  public String getResourceType() {
    return resourceType;
  }

  List<MetaElement> getChildren() {
    return metaResource.getChildren();
  }

  String getResourcesPath() {
    //
    // TODO: Requires access to CrnkBoot.getWebPathPrefix() and anything that might modify a path
    // TODO: alternatively, have a config setting for this generator that essentially duplicates the above
    //
    return "/" + metaResource.getResourcePath();
  }

  String getResourcePath() {
    StringBuilder keyPath = new StringBuilder(getResourcesPath() + "/");
    for (MetaAttribute metaAttribute : metaResource.getPrimaryKey().getElements()) {
      keyPath.append("{");
      keyPath.append(metaAttribute.getName());
      keyPath.append("}");
    }
    return keyPath.toString();
  }

  String getFieldPath(OASResource relatedOasResource) {
    return getResourcePath() + relatedOasResource.getResourcesPath();
  }

  // PARAMETERS

  String getRelationshipsPath(OASResource relatedOasResource) {
    return getResourcePath() + "/relationships" + relatedOasResource.getResourcesPath();
  }

  // OPERATIONS

  Map<OperationType, Operation> generateResourcesOperations() {
    Map<OperationType, Operation> operations = new HashMap<>();
    Resources resourcesPath = new Resources(this.metaResource);
    if (metaResource.isReadable()) {
      operations.put(OperationType.GET, resourcesPath.Get());
    }
    if (metaResource.isInsertable()) {
      operations.put(OperationType.POST, resourcesPath.Post());
    }
    return operations;
  }

  Map<OperationType, Operation> generateResourceOperations() {
    Map<OperationType, Operation> operations = new HashMap<>();
    Resource resourcePath = new Resource(this.metaResource);
    if (metaResource.isReadable()) {
      operations.put(OperationType.GET, resourcePath.Get());
    }
    if (metaResource.isUpdatable()) {
      operations.put(OperationType.PATCH, resourcePath.Patch());
    }
    if (metaResource.isDeletable()) {
      operations.put(OperationType.DELETE, resourcePath.Delete());
    }
    return operations;
  }

  Map<OperationType, Operation> generateFieldOperationsForField(MetaResource relatedMetaResource, MetaResourceField mrf) {
    Map<OperationType, Operation> operations = new HashMap<>();
    Field fieldPath = new Field(this.metaResource, relatedMetaResource, mrf);
    if (metaResource.isReadable() && mrf.isReadable()) {
      operations.put(OperationType.GET, fieldPath.Get());
    }
    if (metaResource.isReadable() && mrf.isInsertable()) {
      operations.put(OperationType.POST, fieldPath.Post());
    }
    if (metaResource.isReadable() && mrf.isUpdatable()) {
      operations.put(OperationType.PATCH, fieldPath.Patch());
    }
    // If the relationship is updatable then we imply that it is deletable.
    if (metaResource.isReadable() && mrf.isUpdatable()) {
      operations.put(OperationType.DELETE, fieldPath.Delete());
    }
    return operations;
  }

  Map<OperationType, Operation> generateRelationshipsOperationsForField(MetaResource relatedMetaResource, MetaResourceField mrf) {
    Map<OperationType, Operation> operations = new HashMap<>();
    Relationship relationshipPath = new Relationship(this.metaResource, relatedMetaResource, mrf);
    if (metaResource.isReadable() && mrf.isReadable()) {
      operations.put(OperationType.GET, relationshipPath.Get());
    }
    if (metaResource.isReadable() && mrf.isInsertable()) {
      operations.put(OperationType.POST, relationshipPath.Post());
    }
    if (metaResource.isReadable() && mrf.isUpdatable()) {
      operations.put(OperationType.PATCH, relationshipPath.Patch());
    }
    // If the relationship is updatable then we imply that it is deletable.
    if (metaResource.isReadable() && mrf.isUpdatable()) {
      operations.put(OperationType.DELETE, relationshipPath.Delete());
    }
    return operations;
  }
}
