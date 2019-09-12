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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.stream.Collectors.joining;

public class OASResource {
  private MetaResource metaResource;
  private String resourceName;
  private String resourceType;
  private Map<String, Schema> attributes;
  private Map<String, Schema> patchAttributes;
  private Map<String, Schema> postAttributes;

  public OASResource(MetaResource metaResource) {
    this.metaResource = metaResource;
    resourceName = metaResource.getName();
    resourceType = metaResource.getResourceType();
    // Create Component
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

  public Map<String, Schema> getAttributes() {
    return attributes;
  }

  public Map<String, Schema> getPatchAttributes() {
    return patchAttributes;
  }

  public Map<String, Schema> getPostAttributes() {
    return postAttributes;
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

 	public Schema resourceListResponse() {
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

  private Operation addFilters(MetaResource metaResource, Operation operation) {
 		// TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
 		operation.getParameters().add(new Parameter().$ref("#/components/parameters/filter"));

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

  public ApiResponse getSingleResponse() {
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

  public ApiResponse getListResponse() {
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
     Map<String, ApiResponse> responses = new TreeMap<String, ApiResponse>(){};

 		responses.put("202", new ApiResponse().$ref("202"));
 		responses.put("204", new ApiResponse().$ref("204"));

 		Map<String, ApiResponse> apiResponseCodes = generateStandardApiErrorResponses();
 		for (Map.Entry<String, ApiResponse> entry : apiResponseCodes.entrySet()) {

 			// TODO: Check to see (somehow) if the metaResource returns this response code
 			// Add reference to error response stored in #/components/responses/<HttpCode>
 			responses.put(entry.getKey(), new ApiResponse().$ref(entry.getKey()));
 		}

 		// Todo: Standard wrapper responses for single & multiple records
 		// responses...

 		return responses;
 	}

  /*
 		Using Crnks list of HTTP status codes, generate standard responses
 		for all statuses in the error range. These ApiResponses will be shared
 		across all endpoints.

 		See "Reusing Responses" https://swagger.io/docs/specification/describing-responses/
 	 */
 	private Map<String, ApiResponse> generateStandardApiErrorResponses() {
 		Map<String, ApiResponse> responses = new LinkedHashMap<>();

 		List<Integer> responseCodes = getStandardHttpStatusCodes();
 		for (Integer responseCode : responseCodes) {
 			if (responseCode >= 400 && responseCode <= 599) {
 				ApiResponse apiResponse = new ApiResponse();
 				apiResponse.description(HttpStatus.toMessage(responseCode));
 				apiResponse.content(new Content()
 						.addMediaType("application/json",
 								new MediaType().schema(new Schema().$ref("ApiError")))
 				);
 				responses.put(responseCode.toString(), apiResponse);
 			}
 		}

 		return responses;
 	}
  /*
 		Crnk maintains a list of HTTP status codes in io.crnk.core.engine.http.HttpStatus
 		as static fields. Iterate through and collect them into a list for use elsewhere.
 	 */
 	private List<Integer> getStandardHttpStatusCodes() {
 		List<Integer> responseCodes = new ArrayList<>();

 		Field[] fields = HttpStatus.class.getDeclaredFields();
 		for (Field f : fields) {
 			if (Modifier.isStatic(f.getModifiers())) {
 				try {
 					responseCodes.add(f.getInt(null));
 				} catch (IllegalAccessException ignore) {
 				}
 			}
 		}
 		return responseCodes;
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
                     .$ref("#/components/parameters/contentType"))));
 	}

  public Operation generateDefaultGetListOperation(MetaResource metaResource) {
 		Operation operation = generateDefaultOperation();
     operation.setDescription("Retrieve a List of " + metaResource.getResourceType() + " resources");
     Map<String, ApiResponse> responses = generateDefaultResponsesMap();
     responses.put("200", new ApiResponse().$ref(metaResource.getName() + "ListResponse"));
     operation.setResponses(OASUtils.apiResponsesFromMap(responses));

 		// Add filters for resource
 		addFilters(metaResource, operation);

 		// Add fields[resource] parameter
 		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + metaResource.getResourceType() + "Fields"));

 		// Add include parameter
 		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + metaResource.getResourceType() + "Include"));

 		// Add sort parameter
 		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + metaResource.getResourceType() + "Sort"));

 		// Add page[limit] parameter
 		operation.getParameters().add(new Parameter().$ref("#/components/parameters/pageLimit"));

 		// Add page[offset] parameter
 		operation.getParameters().add(new Parameter().$ref("#/components/parameters/pageOffset"));

 		return operation;
 	}

  public Operation generateDefaultPostListOperation() {
 	  Operation operation = generateDefaultOperation();
     operation.setDescription("Create a " + metaResource.getName());
     Map<String, ApiResponse> responses = generateDefaultResponsesMap();
     responses.put("201", new ApiResponse()
         .description("Created")
         .content(new Content()
             .addMediaType("application/vnd.api+json",
                 new MediaType().schema(new Schema()
                     .$ref(metaResource.getName() + "Response")))));
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
 																		.$ref(metaResource.getName() + "Post")))));


 		return operation;
 	}

  public Operation generateDefaultGetSingleOperation(MetaResource metaResource) {
 		Operation operation = generateDefaultOperation();
 		operation.setDescription("Retrieve a " + metaResource.getResourceType() + " resource");
 		Map<String, ApiResponse> responses = generateDefaultResponsesMap();
     responses.put("200", new ApiResponse().$ref(metaResource.getName() + "Response"));
 		operation.setResponses(OASUtils.apiResponsesFromMap(responses));

 		operation.getParameters().add(getPrimaryKeyParameter());
 		// Add fields[resource] parameter
 		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + metaResource.getResourceType() + "Fields"));
 		// Add include parameter
 		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + metaResource.getResourceType() + "Include"));

 		return operation;
 	}

  public Operation generateDefaultPatchSingleOperation() {
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

  public Operation generateDefaultDeleteSingleOperation() {
 		Operation operation = generateDefaultOperation();
     operation.setDescription("Delete a " + resourceName);
     Map<String, ApiResponse> responses = generateDefaultResponsesMap();
     responses.put("200", new ApiResponse().description("OK"));
     operation.setResponses(OASUtils.apiResponsesFromMap(responses));
 		operation.getParameters().add(getPrimaryKeyParameter());
 		return operation;
 	}

  public Operation generateDefaultGetRelationshipsOperation(MetaResource relatedMetaResource, boolean oneToMany) {
 		Operation operation = generateDefaultOperation();
 		operation.getParameters().add(getPrimaryKeyParameter());

 		// TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
 		// Add filter[<>] parameters
 		// Only the most basic filters are documented
 		if (oneToMany) {
       addFilters(relatedMetaResource, operation);
     }
 		// Add fields[resource] parameter
 		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + relatedMetaResource.getResourceType() + "Fields"));
 		// Add include parameter
 		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + relatedMetaResource.getResourceType() + "Include"));

 		return operation;
 	}


 	public Operation generateDefaultRelationshipOperation(MetaResource relatedMetaResource, boolean oneToMany, boolean includeBody) {
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
 																		.$ref(relatedMetaResource.getName() + postFix)))));
 		return operation;
 	}



}
