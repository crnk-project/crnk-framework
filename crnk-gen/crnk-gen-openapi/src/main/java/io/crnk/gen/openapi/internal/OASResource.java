package io.crnk.gen.openapi.internal;

import io.crnk.gen.openapi.internal.parameters.*;
import io.crnk.gen.openapi.internal.paths.Field;
import io.crnk.gen.openapi.internal.paths.Relationship;
import io.crnk.gen.openapi.internal.paths.Resource;
import io.crnk.gen.openapi.internal.paths.Resources;
import io.crnk.gen.openapi.internal.responses.RelationshipMultiResponse;
import io.crnk.gen.openapi.internal.responses.RelationshipSingleResponse;
import io.crnk.gen.openapi.internal.responses.ResourceResponse;
import io.crnk.gen.openapi.internal.responses.ResourcesResponse;
import io.crnk.gen.openapi.internal.schemas.*;
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
  private MetaResource metaResource;
  public final String resourceName;
  public final String resourceType;
  private Map<String, Schema> attributes;
  private Map<String, Schema> patchAttributes;
  private Map<String, Schema> postAttributes;
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

  private void initializeAttributes() {
    attributes = new HashMap<>();
    patchAttributes = new HashMap<>();
    postAttributes = new HashMap<>();
    for (MetaElement child : metaResource.getChildren()) {
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
        attributes.put(mrf.getName(), attributeSchema);
        if (((MetaResourceField) child).isUpdatable()) {
          patchAttributes.put(mrf.getName(), attributeSchema);
        }
        if (((MetaResourceField) child).isInsertable()) {
          postAttributes.put(mrf.getName(), attributeSchema);
        }
      }
    }
  }

  private void initializeComponentParameters() {
    componentParameters = new HashMap<>();
    componentParameters.put(resourceType + "PrimaryKey", new PrimaryKey(metaResource).parameter());
    componentParameters.put(resourceType + "Fields", new Fields(metaResource).parameter());
    componentParameters.put(resourceType + "Include", new Include(metaResource).parameter());
    componentParameters.put(resourceType + "Sort", new Sort(metaResource).parameter());
  }

  private void initializeComponentSchemas() {
    componentSchemas = new HashMap<>();
    componentSchemas.put(resourceType + "Reference", new ResourceReference(metaResource).schema());
    componentSchemas.put(resourceType + "Attributes", new ResourceAttributes(metaResource).schema());
    componentSchemas.put(resourceType + "PostAttributes", new ResourcePostAttributes(metaResource).schema());
    componentSchemas.put(resourceType + "PatchAttributes", new ResourcePatchAttributes(metaResource).schema());
    componentSchemas.put(resourceName, new ResourceSchema(metaResource).schema());
    componentSchemas.put(resourceName + "Patch", new PatchResource(metaResource).schema());
    componentSchemas.put(resourceName + "Post", new PostResource(metaResource).schema());
    componentSchemas.put(resourceName + "Response", new ResourceResponseSchema(metaResource).schema());
    componentSchemas.put(resourceName + "ListResponse", new ResourcesResponseSchema(metaResource).schema());
    componentSchemas.put(resourceName + "Relationship", new RelationshipSingleResponseSchema(metaResource).schema());
    componentSchemas.put(resourceName + "Relationships", new RelationshipMultiResponseSchema(metaResource).schema());
  }

  private void initializeComponentResponses() {
    componentResponses = new HashMap<>();
    componentResponses.put(resourceName + "Response", new ResourceResponse(metaResource).response());
    componentResponses.put(resourceName + "ListResponse", new ResourcesResponse(metaResource).response());
    componentResponses.put(resourceName + "RelationshipResponse", new RelationshipSingleResponse(metaResource).response());
    componentResponses.put(resourceName + "RelationshipsResponse", new RelationshipMultiResponse(metaResource).response());
  }

  public Map<String, Schema> getAttributes() {
    return attributes;
  }

  public Map<String, Schema> getPatchAttributes() {
    return patchAttributes;
  }

  public Map<String, Schema> getPostAttributes() {
    return postAttributes;
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

  public String getResourceName() {
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

  String getRelationshipsPath(OASResource relatedOasResource) {
    return getResourcePath() + "/relationships" + relatedOasResource.getResourcesPath();
  }

  // PARAMETERS

  public Operation addFilters(Operation operation) {
    // TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/Filter"));

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

  // OPERATIONS

  Map<OperationType, Operation> generateResourcesOperations() {
    Map<OperationType, Operation> operations = new HashMap<>();
    Resources resourcesPath = new Resources(this);
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
    Resource resourcePath = new Resource(this);
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

  Map<OperationType, Operation> generateFieldOperationsForField(OASResource relatedOasResource, MetaResourceField mrf) {
    Map<OperationType, Operation> operations = new HashMap<>();
    Field fieldPath = new Field(this, relatedOasResource, mrf);
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

  Map<OperationType, Operation> generateRelationshipsOperationsForField(OASResource relatedOasResource, MetaResourceField mrf) {
    Map<OperationType, Operation> operations = new HashMap<>();
    Relationship relationshipPath = new Relationship(this, relatedOasResource, mrf);
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
