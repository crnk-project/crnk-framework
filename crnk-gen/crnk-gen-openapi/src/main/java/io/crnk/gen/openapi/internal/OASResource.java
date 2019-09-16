package io.crnk.gen.openapi.internal;

import io.crnk.core.engine.http.HttpStatus;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.*;

import static java.util.stream.Collectors.joining;

public class OASResource {
  private MetaResource metaResource;
  private String resourceName;
  private String resourceType;
  private Map<String, Schema> attributes;
  private Map<String, Schema> patchAttributes;
  private Map<String, Schema> postAttributes;
  private Map<String, Parameter> componentParameters;
  private Map<String, Schema> componentSchemas;
  private Map<String, ApiResponse> componentResponses;

  public OASResource(MetaResource metaResource) {
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
    componentParameters.put(resourceType + "Fields", generateDefaultFieldsParameter());
    componentParameters.put(resourceType + "Include", generateDefaultIncludeParameter());
    componentParameters.put(resourceType + "Sort", generateDefaultSortParameter());
  }

  private void initializeComponentSchemas() {
    componentSchemas = new HashMap<>();
    componentSchemas.put(resourceType + "Reference", resourceReference());
    componentSchemas.put(resourceName, resource());
    componentSchemas.put(resourceName + "Patch", patchResourceRequestBody());
    componentSchemas.put(resourceName + "Post", postResourceRequestBody());
    componentSchemas.put(resourceName + "Response", resourceResponse());
    componentSchemas.put(resourceName + "ListResponse", resourcesResponse());
    componentSchemas.put(resourceName + "Relationship", singleRelationshipBody());
    componentSchemas.put(resourceName + "Relationships", multiRelationshipBody());
  }

  private void initializeComponentResponses() {
    componentResponses = new HashMap<>();
    componentResponses.put(resourceName + "Response", getResourceResponse());
    componentResponses.put(resourceName + "ListResponse", getResourcesResponse());
    componentResponses.put(resourceName + "RelationshipResponse", getRelationshipResponse());
    componentResponses.put(resourceName + "RelationshipsResponse", getRelationshipsResponse());
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

  public Map<String, Parameter> getComponentParameters() {
    return componentParameters;
  }

  public Map<String, Schema> getComponentSchemas() {
    return componentSchemas;
  }

  public Map<String, ApiResponse> getComponentResponses() {
    return componentResponses;
  }

  public String getResourceName() {
    return resourceName;
  }

  public String getResourceType() {
    return resourceType;
  }

  public List<MetaElement> getChildren() {
    return metaResource.getChildren();
  }

  public String getResourcesPath() {
    //
    // TODO: Requires access to CrnkBoot.getWebPathPrefix() and anything that might modify a path
    // TODO: alternatively, have a config setting for this generator that essentially duplicates the above
    //
    return "/" + metaResource.getResourcePath();
  }

  public String getResourcePath() {
    StringBuilder keyPath = new StringBuilder(getResourcesPath() + "/");
    for (MetaAttribute metaAttribute : metaResource.getPrimaryKey().getElements()) {
      keyPath.append("{");
      keyPath.append(metaAttribute.getName());
      keyPath.append("}");
    }
    return keyPath.toString();
  }

  public String getFieldPath(OASResource relatedOasResource) {
    return getResourcePath() + relatedOasResource.getResourcesPath();
  }

  public String getRelationshipsPath(OASResource relatedOasResource) {
    return getResourcePath() + "/relationships" + relatedOasResource.getResourcesPath();
  }

  // SCHEMAS

  public Schema resourceReference() {
    return new Schema()
        .type("object")
        .addProperties(
            "type",
            typeSchema(resourceName))
        .addProperties(
            "id",
            new Schema()
                .type("string")
                .description("The JSON:API resource ID"))
        .required(Arrays.asList("id", "type"));
  }

  public Schema resource() {
    //Defines a schema for a JSON-API resource, without the enclosing top-level document.
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                OASUtils.get$refSchema(resourceType + "Reference"),
                new Schema()
                    .type("object")
                    .addProperties(
                        "relationships",
                        new Schema()
                            .type("object"))
                    .addProperties(
                        "links",
                        new Schema()
                            .type("object"))
                    .addProperties(
                        "attributes",
                        new Schema()
                            .type("object")
                            .properties(attributes))
                    .required(Collections.singletonList("attributes"))));
  }

  public Schema resourceResponse() {
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                OASUtils.get$refSchema("ResponseMixin"),
                new Schema()
                    .addProperties(
                        "data",
                        new ArraySchema()
                            .items(
                                OASUtils.get$refSchema(resourceName)))
                    .required(Collections.singletonList("data"))));
  }

  public Schema resourcesResponse() {
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                OASUtils.get$refSchema("ListResponseMixin"),
                new Schema()
                    .addProperties(
                        "data",
                        new ArraySchema()
                            .items(
                                OASUtils.get$refSchema(resourceName)))
                    .required(Collections.singletonList("data"))));
  }

  // SCHEMAS FOR RELATIONSHIPS

  public Schema singleRelationshipBody() {
    //Defines a schema for the PATCH parameters of a JSON:API resource
    return new ObjectSchema()
        .addProperties(
            "data",
            OASUtils.get$refSchema(resourceType + "Reference"));
  }

  public Schema multiRelationshipBody() {
    //Defines a schema for the PATCH parameters of a JSON:API resource
    return new ObjectSchema()
        .addProperties(
            "data",
            new ArraySchema()
                .items(OASUtils.get$refSchema(resourceType + "Reference")));
  }


  private Schema typeSchema(String typeName) {
    Schema typeSchema = new StringSchema().description("The JSON:API resource type (" + typeName + ")");
    typeSchema.addEnumItemObject(typeName);
    return typeSchema;
  }

  // PARAMETERS

  public Parameter generateDefaultFieldsParameter() {
    return new Parameter()
        .name("fields[" + resourceType + "]")
        .description(resourceType + " fields to include (csv)")
        .in("query")
        .schema(new StringSchema()
            ._default(
                metaResource
                    .getAttributes()
                    .stream()
                    .map(MetaElement::getName)
                    .collect(joining(","))));
  }

  public Parameter generateDefaultIncludeParameter() {
    return new Parameter()
        .name("include")
        .description(resourceType + " relationships to include (csv)")
        .in("query")
        .schema(new StringSchema()
            ._default(
                metaResource
                    .getAttributes()
                    .stream()
                    .filter(MetaAttribute::isAssociation)
                    .map(e -> e.getType().getElementType().getName())
                    .collect(joining(","))));
  }

  public Parameter generateDefaultSortParameter() {
    return new Parameter()
        .name("sort")
        .description(resourceType + " sort order (csv)")
        .in("query")
        .schema(new StringSchema()
            .example(
                metaResource
                    .getAttributes()
                    .stream()
                    .filter(MetaAttribute::isSortable)
                    .map(MetaElement::getName)
                    .collect(joining(","))));
  }

  private Parameter getPrimaryKeyParameter() {
    Parameter parameter = new Parameter();
    for (MetaElement metaElement : metaResource.getChildren()) {
      if (metaElement instanceof MetaAttribute) {
        MetaAttribute metaAttribute = (MetaAttribute) metaElement;
        if (metaAttribute.isPrimaryKeyAttribute()) {
          parameter = parameter
              .name(metaElement.getName())
              .in("path")
              .schema(OASUtils.transformMetaResourceField(((MetaAttribute) metaElement).getType()));
        }
      }
    }
    return parameter;
  }

  private Operation addFilters(Operation operation) {
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
          operation.getParameters().add(
              new Parameter()
                  .name("filter[" + child.getName() + "]")
                  .description("Filter by " + child.getName() + " (csv)")
                  .in("query")
                  .schema(new StringSchema())
          );
        }
      }
    }
    return operation;
  }

  // REQUEST BODIES

  public Schema patchResourceRequestBody() {
    return resourceRequestBody(patchAttributes);
  }

  public Schema postResourceRequestBody() {
    return resourceRequestBody(postAttributes);
  }

  private Schema resourceRequestBody(Map<String, Schema> attributes) {
    //Defines a schema for the PATCH parameters of a JSON:API resource
    return new ComposedSchema()
        .allOf(
            Arrays.asList(
                OASUtils.get$refSchema(resourceType + "Reference"),
                new Schema()
                    .type("object")
                    .addProperties(
                        "attributes",
                        new Schema()
                            .type("object")
                            .properties(attributes))));
  }

  // RESPONSES

  public ApiResponse getResourceResponse() {
    return new ApiResponse()
        .description(HttpStatus.toMessage(200))
        .content(
            new Content()
                .addMediaType(
                    "application/json",
                    new MediaType()
                        .schema(
                            new Schema()
                                .$ref(resourceName + "Response"))));
  }

  public ApiResponse getResourcesResponse() {
    return new ApiResponse()
        .description(HttpStatus.toMessage(200))
        .content(
            new Content()
                .addMediaType(
                    "application/json",
                    new MediaType()
                        .schema(
                            new Schema()
                                .$ref(resourceName + "ListResponse"))));
  }

  public Map<String, ApiResponse> generateDefaultResponsesMap() {
    Map<String, ApiResponse> responses = new TreeMap<String, ApiResponse>() {
    };

    responses.put("202", new ApiResponse().$ref("202"));
    responses.put("204", new ApiResponse().$ref("204"));

    Map<String, ApiResponse> apiResponseCodes = OASErrors.generateStandardApiErrorResponses();
    for (Map.Entry<String, ApiResponse> entry : apiResponseCodes.entrySet()) {

      // TODO: Check to see (somehow) if the metaResource returns this response code
      // Add reference to error response stored in #/components/responses/<HttpCode>
      responses.put(entry.getKey(), new ApiResponse().$ref(entry.getKey()));
    }

    // Todo: Standard wrapper responses for single & multiple records
    // responses...

    return responses;
  }

  // RESPONSES - RELATIONSHIPS

  public ApiResponse getRelationshipResponse() {
    return new ApiResponse()
        .description(HttpStatus.toMessage(200))
        .content(
            new Content()
                .addMediaType(
                    "application/json",
                    new MediaType()
                        .schema(
                            new Schema()
                                .$ref(resourceName + "Relationship"))));
  }

  public ApiResponse getRelationshipsResponse() {
    return new ApiResponse()
        .description(HttpStatus.toMessage(200))
        .content(
            new Content()
                .addMediaType(
                    "application/json",
                    new MediaType()
                        .schema(
                            new Schema()
                                .$ref(resourceName + "Relationships"))));
  }

  // OPERATIONS

  private Operation generateDefaultOperation() {
    return new Operation().parameters(
        new ArrayList<>(
            Collections.singletonList(
                new Parameter()
                    .$ref("#/components/parameters/ContentType"))));
  }

  public Map<OperationType, Operation> generateResourcesOperations() {
    Map<OperationType, Operation> operations = new HashMap<>();
    if (metaResource.isReadable()) {
      operations.put(OperationType.GET, generateGetResourcesOperation());
    }
    if (metaResource.isInsertable()) {
      operations.put(OperationType.POST, generatePostResourcesOperation());
    }
    return operations;
  }

  public Operation generateGetResourcesOperation() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Retrieve a List of " + resourceType + " resources");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ApiResponse().$ref(resourceName + "ListResponse"));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));

    // Add filters for resource
    addFilters(operation);

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

  public Operation generatePostResourcesOperation() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Create a " + resourceName);
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("201", new ApiResponse()
        .description("Created")
        .content(new Content()
            .addMediaType("application/vnd.api+json",
                new MediaType().schema(new Schema()
                    .$ref(resourceName + "Response")))));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));
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

  public Map<OperationType, Operation> generateResourceOperations() {
    Map<OperationType, Operation> operations = new HashMap<>();
    if (metaResource.isReadable()) {
      operations.put(OperationType.GET, generateGetResourceOperation());
    }
    if (metaResource.isUpdatable()) {
      operations.put(OperationType.PATCH, generatePatchResourceOperation());
    }
    if (metaResource.isDeletable()) {
      operations.put(OperationType.DELETE, generateDeleteResourceOperation());
    }
    return operations;
  }

  public Operation generateGetResourceOperation() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Retrieve a " + resourceType + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ApiResponse().$ref(resourceName + "Response"));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));

    operation.getParameters().add(getPrimaryKeyParameter());
    // Add fields[resource] parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + resourceType + "Fields"));
    // Add include parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + resourceType + "Include"));

    return operation;
  }

  public Operation generatePatchResourceOperation() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Update a " + resourceName);
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ApiResponse()
        .description("OK")
        .content(new Content()
            .addMediaType("application/vnd.api+json",
                new MediaType().schema(new Schema()
                    .$ref(resourceName + "Response")))));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));
    operation.getParameters().add(getPrimaryKeyParameter());
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType(
                        "application/json",
                        new MediaType()
                            .schema(
                                new Schema()
                                    .$ref(resourceName + "Patch")))));
    return operation;
  }

  public Operation generateDeleteResourceOperation() {
    Operation operation = generateDefaultOperation();
    operation.setDescription("Delete a " + resourceName);
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    responses.put("200", new ApiResponse().description("OK"));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));
    operation.getParameters().add(getPrimaryKeyParameter());

    return operation;
  }

  public Map<OperationType, Operation> generateFieldOperationsForField(OASResource relatedOasResource, MetaResourceField mrf) {
    Map<OperationType, Operation> operations = new HashMap<>();
    if (metaResource.isReadable() && mrf.isReadable()) {
      operations.put(OperationType.GET, generateGetFieldOperation(relatedOasResource, mrf));
    }
    if (metaResource.isReadable() && mrf.isInsertable()) {
      operations.put(OperationType.POST, generatePostFieldOperation(relatedOasResource, mrf));
    }
    if (metaResource.isReadable() && mrf.isUpdatable()) {
      operations.put(OperationType.PATCH, generatePatchFieldOperation(relatedOasResource, mrf));
    }
    // If the relationship is updatable then we imply that it is deletable.
    if (metaResource.isReadable() && mrf.isUpdatable()) {
      operations.put(OperationType.DELETE, generateDeleteFieldOperation(relatedOasResource, mrf));
    }
    return operations;
  }

  public Operation generateGetFieldOperation(OASResource relatedOasResource, MetaResourceField mrf) {
    Operation operation = generateDefaultGetRelationshipsOrFieldsOperation(relatedOasResource, mrf.getType().isCollection() || mrf.getType().isMap());
    operation.setDescription("Retrieve " + relatedOasResource.getResourceType() + " related to a " + resourceType + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = mrf.getType().isCollection() || mrf.getType().isMap() ? "ListResponse" : "Response";
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));

    return operation;
  }


  public Operation generatePostFieldOperation(OASResource relatedOasResource, MetaResourceField mrf) {
    Operation operation = generateDefaultRelationshipOperation(relatedOasResource, mrf.getType().isCollection() || mrf.getType().isMap(), true);
    operation.setDescription("Create " + resourceType + " relationship to a " + relatedOasResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix + "Response"));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));

    return operation;
  }

  public Operation generatePatchFieldOperation(OASResource relatedOasResource, MetaResourceField mrf) {
    Operation operation = generateDefaultRelationshipOperation(relatedOasResource, mrf.getType().isCollection() || mrf.getType().isMap(), true);
    operation.setDescription("Update " + resourceType + " relationship to a " + relatedOasResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix + "Response"));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));

    return operation;
  }

  public Operation generateDeleteFieldOperation(OASResource relatedOasResource, MetaResourceField mrf) {
    Operation operation = generateDefaultRelationshipOperation(relatedOasResource, mrf.getType().isCollection() || mrf.getType().isMap(), false);
    operation.setDescription("Delete " + resourceType + " relationship to a " + relatedOasResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
    // TODO: OpenAPI does not allow DELETE methods to define a RequestBody (https://github.com/OAI/OpenAPI-Specification/issues/1801)
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix + "Response"));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));

    return operation;
  }

  public Map<OperationType, Operation> generateRelationshipsOperationsForField(OASResource relatedOasResource, MetaResourceField mrf) {
    Map<OperationType, Operation> operations = new HashMap<>();
    if (metaResource.isReadable() && mrf.isReadable()) {
      operations.put(OperationType.GET, generateGetRelationshipsOperation(relatedOasResource, mrf));
    }
    if (metaResource.isReadable() && mrf.isInsertable()) {
      operations.put(OperationType.POST, generatePostRelationshipsOperation(relatedOasResource, mrf));
    }
    if (metaResource.isReadable() && mrf.isUpdatable()) {
      operations.put(OperationType.PATCH, generatePatchRelationshipsOperation(relatedOasResource, mrf));
    }
    // If the relationship is updatable then we imply that it is deletable.
    if (metaResource.isReadable() && mrf.isUpdatable()) {
      operations.put(OperationType.DELETE, generateDeleteRelationshipsOperation(relatedOasResource, mrf));
    }
    return operations;
  }

  public Operation generateGetRelationshipsOperation(OASResource relatedOasResource, MetaResourceField mrf) {
    if (!(metaResource.isReadable() && mrf.isReadable())) {
      return null;
    }
    Operation operation = generateDefaultGetRelationshipsOrFieldsOperation(relatedOasResource, mrf.getType().isCollection() || mrf.getType().isMap());
    operation.setDescription("Retrieve " + relatedOasResource.getResourceType() + " references related to a " + resourceType + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = mrf.getType().isCollection() || mrf.getType().isMap() ? "RelationshipsResponse" : "RelationshipResponse";
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));

    return operation;
  }

  public Operation generatePostRelationshipsOperation(OASResource relatedOasResource, MetaResourceField mrf) {
    if (!(metaResource.isReadable() && mrf.isInsertable())) {
      return null;
    }
    Operation operation = generateDefaultRelationshipOperation(relatedOasResource, mrf.getType().isCollection() || mrf.getType().isMap(), true);
    operation.setDescription("Create " + resourceType + " relationship to a " + relatedOasResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix + "Response"));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));

    return operation;
  }

  public Operation generatePatchRelationshipsOperation(OASResource relatedOasResource, MetaResourceField mrf) {
    if (!(metaResource.isReadable() && mrf.isUpdatable())) {
      return null;
    }
    Operation operation = generateDefaultRelationshipOperation(relatedOasResource, mrf.getType().isCollection() || mrf.getType().isMap(), true);
    operation.setDescription("Update " + resourceType + " relationship to a " + relatedOasResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix + "Response"));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));

    return operation;
  }

  public Operation generateDeleteRelationshipsOperation(OASResource relatedOasResource, MetaResourceField mrf) {
    if (!(metaResource.isReadable() && mrf.isUpdatable())) {
      return null;
    }
    Operation operation = generateDefaultRelationshipOperation(relatedOasResource, mrf.getType().isCollection() || mrf.getType().isMap(), false);
    operation.setDescription("Delete " + resourceType + " relationship to a " + relatedOasResource.getResourceType() + " resource");
    Map<String, ApiResponse> responses = generateDefaultResponsesMap();
    String responsePostfix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
    responses.put("200", new ApiResponse().$ref(relatedOasResource.getResourceName() + responsePostfix + "Response"));
    operation.setResponses(OASUtils.apiResponsesFromMap(responses));

    return operation;
  }


  public Operation generateDefaultGetRelationshipsOrFieldsOperation(OASResource relatedOasResource, boolean oneToMany) {
    Operation operation = generateDefaultOperation();
    operation.getParameters().add(getPrimaryKeyParameter());

    // TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
    // Add filter[<>] parameters
    // Only the most basic filters are documented
    if (oneToMany) {
      addFilters(operation);
    }
    // Add fields[resource] parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + relatedOasResource.getResourceType() + "Fields"));
    // Add include parameter
    operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + relatedOasResource.getResourceType() + "Include"));

    return operation;
  }

  public Operation generateDefaultRelationshipOperation(OASResource relatedOasResource, boolean oneToMany, boolean includeBody) {
    Operation operation = generateDefaultOperation();
    operation.getParameters().add(getPrimaryKeyParameter());
    String postFix = oneToMany ? "Relationships" : "Relationship";
    if (!includeBody) {
      return operation;
    }
    operation.setRequestBody(
        new RequestBody()
            .content(
                new Content()
                    .addMediaType(
                        "application/json",
                        new MediaType()
                            .schema(
                                new Schema()
                                    .$ref(relatedOasResource.getResourceName() + postFix)))));
    return operation;
  }
}
