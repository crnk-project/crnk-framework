package io.crnk.gen.openapi.internal;

import io.crnk.gen.openapi.internal.operations.NestedDelete;
import io.crnk.gen.openapi.internal.operations.NestedGet;
import io.crnk.gen.openapi.internal.operations.NestedPatch;
import io.crnk.gen.openapi.internal.operations.NestedPost;
import io.crnk.gen.openapi.internal.operations.OASOperation;
import io.crnk.gen.openapi.internal.operations.RelationshipDelete;
import io.crnk.gen.openapi.internal.operations.RelationshipGet;
import io.crnk.gen.openapi.internal.operations.RelationshipPatch;
import io.crnk.gen.openapi.internal.operations.RelationshipPost;
import io.crnk.gen.openapi.internal.operations.ResourceDelete;
import io.crnk.gen.openapi.internal.operations.ResourceGet;
import io.crnk.gen.openapi.internal.operations.ResourcePatch;
import io.crnk.gen.openapi.internal.operations.ResourcesGet;
import io.crnk.gen.openapi.internal.operations.ResourcesPost;
import io.crnk.gen.openapi.internal.parameters.FieldFilter;
import io.crnk.gen.openapi.internal.parameters.Fields;
import io.crnk.gen.openapi.internal.parameters.Include;
import io.crnk.gen.openapi.internal.parameters.NestedFilter;
import io.crnk.gen.openapi.internal.parameters.PrimaryKey;
import io.crnk.gen.openapi.internal.parameters.Sort;
import io.crnk.gen.openapi.internal.responses.ResourceReferenceResponse;
import io.crnk.gen.openapi.internal.responses.ResourceReferencesResponse;
import io.crnk.gen.openapi.internal.responses.ResourceResponse;
import io.crnk.gen.openapi.internal.responses.ResourcesResponse;
import io.crnk.gen.openapi.internal.schemas.PatchResource;
import io.crnk.gen.openapi.internal.schemas.PostResource;
import io.crnk.gen.openapi.internal.schemas.PostResourceReference;
import io.crnk.gen.openapi.internal.schemas.ResourceAttribute;
import io.crnk.gen.openapi.internal.schemas.ResourceAttributes;
import io.crnk.gen.openapi.internal.schemas.ResourcePatchAttributes;
import io.crnk.gen.openapi.internal.schemas.ResourcePostAttributes;
import io.crnk.gen.openapi.internal.schemas.ResourceReference;
import io.crnk.gen.openapi.internal.schemas.ResourceReferenceResponseSchema;
import io.crnk.gen.openapi.internal.schemas.ResourceReferencesResponseSchema;
import io.crnk.gen.openapi.internal.schemas.ResourceResponseSchema;
import io.crnk.gen.openapi.internal.schemas.ResourceSchema;
import io.crnk.gen.openapi.internal.schemas.ResourcesResponseSchema;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OASResource {

  private MetaResource metaResource;

  private Map<String, Parameter> componentParameters;

  private Map<String, Schema> componentSchemas;

  private Map<String, ApiResponse> componentResponses;

  private List<OASOperation> operations;

  OASResource(MetaResource metaResource) {
    this.metaResource = metaResource;
    initializeComponentParameters();
    initializeComponentSchemas();
    initializeComponentResponses();
    initializeOperations();
  }

  private void initializeComponentParameters() {
    if (componentParameters != null) {
      return;
    }
    componentParameters = new HashMap<>();
    componentParameters.put(new PrimaryKey(metaResource).getName(), new PrimaryKey(metaResource).parameter());
    componentParameters.put(new Fields(metaResource).getName(), new Fields(metaResource).parameter());
    componentParameters.put(new Include(metaResource).getName(), new Include(metaResource).parameter());
    componentParameters.put(new Sort(metaResource).getName(), new Sort(metaResource).parameter());
  }

  private void initializeComponentSchemas() {
    if (componentSchemas != null) {
      return;
    }
    componentSchemas = OASUtils.attributes(metaResource, true)
        .map(e -> new ResourceAttribute(metaResource, e))
        .collect(Collectors.toMap(ResourceAttribute::getName, ResourceAttribute::schema));

    componentSchemas.put(new PostResourceReference(metaResource).getName(), new PostResourceReference(metaResource).schema());
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
    if (componentResponses != null) {
      return;
    }
    componentResponses = new HashMap<>();
    componentResponses.put(new ResourceResponse(metaResource).getName(), new ResourceResponse(metaResource).response());
    componentResponses.put(new ResourcesResponse(metaResource).getName(), new ResourcesResponse(metaResource).response());
    componentResponses.put(new ResourceReferenceResponse(metaResource).getName(), new ResourceReferenceResponse(metaResource).response());
    componentResponses.put(new ResourceReferencesResponse(metaResource).getName(), new ResourceReferencesResponse(metaResource).response());
  }

  private void initializeOperations() {
    if (operations != null) {
      return;
    }
    operations = Stream.of(
        resourceOperations().stream(),
        resourcesOperations().stream(),
        nestedOperations().stream(),
        relationshipsOperations().stream()
    )
        .reduce(Stream::concat)
        .orElseGet(Stream::empty)
        .filter(OASOperation::isEnabled)
        .collect(Collectors.toList());
  }

  private List<OASOperation> resourceOperations() {
    List<OASOperation> operations = new ArrayList<>();
    operations.add(new ResourceGet(metaResource));
    operations.add(new ResourcePatch(metaResource));
    operations.add(new ResourceDelete(metaResource));
    return operations;
  }

  private List<OASOperation> resourcesOperations() {
    List<OASOperation> operations = new ArrayList<>();
    operations.add(new ResourcesGet(metaResource));
    operations.add(new ResourcesPost(metaResource));
    return operations;
  }

  private List<OASOperation> nestedOperations() {
    MetaResource relatedMetaResource;
    List<OASOperation> operations = new ArrayList<>();
    for (MetaResourceField metaResourceField :
        OASUtils.associationAttributes(metaResource, false)
            .collect(Collectors.toList())) {
      relatedMetaResource = (MetaResource) metaResourceField.getType().getElementType();
      operations.add(new NestedGet(metaResource, metaResourceField, relatedMetaResource));
      operations.add(new NestedDelete(metaResource, metaResourceField, relatedMetaResource));
      operations.add(new NestedPatch(metaResource, metaResourceField, relatedMetaResource));
      operations.add(new NestedPost(metaResource, metaResourceField, relatedMetaResource));
    }
    return operations;
  }

  private List<OASOperation> relationshipsOperations() {
    MetaResource relatedMetaResource;
    List<OASOperation> operations = new ArrayList<>();
    for (MetaResourceField metaResourceField :
        OASUtils.associationAttributes(metaResource, false)
            .collect(Collectors.toList())) {
      relatedMetaResource = (MetaResource) metaResourceField.getType().getElementType();
      operations.add(new RelationshipGet(metaResource, metaResourceField, relatedMetaResource));
      operations.add(new RelationshipDelete(metaResource, metaResourceField, relatedMetaResource));
      operations.add(new RelationshipPatch(metaResource, metaResourceField, relatedMetaResource));
      operations.add(new RelationshipPost(metaResource, metaResourceField, relatedMetaResource));
    }
    return operations;
  }

  public static Operation addFilters(MetaResource metaResource, Operation operation) {
    // TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
    List<Parameter> parameters = operation.getParameters();
    parameters.add(new NestedFilter().$ref());

    // Add filter[<>] parameters
    // Only the most basic filters are documented
    OASUtils.filterAttributes(metaResource, true)
        .forEach(e -> parameters.add(new FieldFilter(metaResource, e).parameter()));
    return operation;
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

  List<OASOperation> getOperations() {
    return operations;
  }

  String getResourceName() {
    return metaResource.getName();
  }

  public String getResourceType() {
    return metaResource.getResourceType();
  }
}
