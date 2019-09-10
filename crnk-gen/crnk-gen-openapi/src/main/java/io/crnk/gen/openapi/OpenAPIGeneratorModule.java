package io.crnk.gen.openapi;

import io.crnk.core.engine.http.HttpStatus;
import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.*;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;

import static java.util.stream.Collectors.joining;


public class OpenAPIGeneratorModule implements GeneratorModule {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIGeneratorModule.class);
	private static final String NAME = "openapi";
	private OpenAPIGeneratorConfig config = new OpenAPIGeneratorConfig();
	private ClassLoader classloader;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void generate(Object meta) throws IOException {
		LOGGER.info("performing openapi generation");
		OpenAPI openApi = config.getOpenAPI();

		openApi.getComponents().schemas(generateDefaultSchemas());
		openApi.getComponents().responses(getStandardApiErrorResponses());
		openApi.getComponents().parameters(getStandardPagingParameters());
		openApi.getComponents().getResponses().put("AcceptedResponse", new ApiResponse()
				.description("Accepted")
				.content(new Content()
						.addMediaType("application/json",
								new MediaType().schema(new Schema()
										.$ref("Accepted"))))
		);
		// TODO: Respect @JsonApiExposed(false)
		MetaLookup metaLookup = (MetaLookup) meta;
		List<MetaResource> metaResources = getJsonApiResources(metaLookup);
		for (MetaResource metaResource : metaResources) {
			PathItem listPathItem = openApi.getPaths().getOrDefault(getApiPath(metaResource), new PathItem());
			PathItem singlePathItem = openApi.getPaths().getOrDefault(getApiPath(metaResource) + getPrimaryKeyPath(metaResource), new PathItem());

			// Create Component
			Map<String, Schema> attributes = new HashMap<>();
			Map<String, Schema> patchAttributes = new HashMap<>();
			Map<String, Schema> postAttributes = new HashMap<>();
			for (MetaElement child : metaResource.getChildren()) {
				if (child == null) {
					continue;
				} else if (child instanceof MetaPrimaryKey) {
					continue;
				} else if (((MetaResourceField) child).isPrimaryKeyAttribute()) {
					continue;
				} else if (child instanceof MetaResourceField) {
					MetaResourceField mrf = (MetaResourceField) child;
					Schema attributeSchema = transformMetaResourceField(mrf.getType());
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

			// Add Fields Parameter
			Parameter fieldsParameter = generateDefaultFieldsParameter(metaResource);
			openApi.getComponents().addParameters(metaResource.getResourceType() + "Fields", fieldsParameter);

			// Add Include Parameter
			Parameter includeParameter = generateDefaultIncludeParameter(metaResource);
			openApi.getComponents().addParameters(metaResource.getResourceType() + "Include", includeParameter);

			// Add Sort parameter
			Parameter sortParameter = generateDefaultSortParameter(metaResource);
			openApi.getComponents().addParameters(metaResource.getResourceType() + "Sort", sortParameter);

			// Add ReferenceType Schema
			Schema resourceReference = resourceReference(metaResource.getName());
			openApi.getComponents().addSchemas(metaResource.getResourceType() + "Reference", resourceReference);

			// Add Types Schema
			Schema resource = resource(metaResource.getResourceType(), attributes);
			openApi.getComponents().addSchemas(metaResource.getName(), resource);

			// Add PATCH Resource Schema
			Schema patchResourceBody = resourceRequestBody(metaResource.getResourceType(), patchAttributes);
			openApi.getComponents().addSchemas(metaResource.getName() + "Patch", patchResourceBody);

			// Add POST Resource Schema
			Schema postResourceBody = resourceRequestBody(metaResource.getResourceType(), postAttributes);
			openApi.getComponents().addSchemas(metaResource.getName() + "Post", postResourceBody);

			// Add relationship modification request body
			Schema singleRelationshipBody = singleRelationshipBody(metaResource.getResourceType());
			openApi.getComponents().addSchemas(metaResource.getName() + "Relationship", singleRelationshipBody);
			openApi.getComponents().addResponses(metaResource.getName() + "RelationshipResponse", getRelationshipResponse(metaResource.getName()));

			// Add relationships modification request body
			Schema multiRelationshipBody = multiRelationshipBody(metaResource.getResourceType());
			openApi.getComponents().addSchemas(metaResource.getName() + "Relationships", multiRelationshipBody);
			openApi.getComponents().addResponses(metaResource.getName() + "RelationshipsResponse", getRelationshipsResponse(metaResource.getName()));

			// Add Response Schema
			Schema resourceResponse = resourceResponse(metaResource.getName());
			openApi.getComponents().addSchemas(metaResource.getName() + "Response", resourceResponse);
			openApi.getComponents().addResponses(metaResource.getName() + "Response", getSingleResponse(metaResource.getName()));

			// Add ListResponse Schema
			Schema resourceListResponse = resourceListResponse(metaResource.getName());
			openApi.getComponents().addSchemas(metaResource.getName() + "ListResponse", resourceListResponse);
			openApi.getComponents().addResponses(metaResource.getName() + "ListResponse", getListResponse(metaResource.getName()));

			// Relationships can be accessed in 2 ways:
			//  1.	/api/A/1/b  								The full related resource
			// TODO  2.	/api/A/1/relationships/b		The "ids" as belong to the resource
			if (metaResource.isReadable()) {
				// List Response
				Operation getListOperation = generateDefaultGetListOperation(metaResource);
				getListOperation.setDescription("Retrieve a List of " + metaResource.getResourceType() + " resources");
				listPathItem.setGet(mergeOperations(getListOperation, listPathItem.getGet()));
				ApiResponses getListResponses = generateDefaultResponses(metaResource);
				getListResponses.addApiResponse("200", new ApiResponse().$ref(metaResource.getName() + "ListResponse"));
				getListOperation.setResponses(getListResponses);
				openApi.getPaths().addPathItem(getApiPath(metaResource), listPathItem);

				// Single Response
				Operation getSingleOperation = generateDefaultGetSingleOperation(metaResource);
				getSingleOperation.setDescription("Retrieve a " + metaResource.getResourceType() + " resource");
				singlePathItem.setGet(mergeOperations(getSingleOperation, singlePathItem.getGet()));
				ApiResponses getSingleResponses = generateDefaultResponses(metaResource);
				getSingleResponses.addApiResponse("200", new ApiResponse().$ref(metaResource.getName() + "Response"));
				getSingleOperation.setResponses(getSingleResponses);
				openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource), singlePathItem);

				// Generate GET Operations for /api/A/1/B relationship path
				for (MetaElement child : metaResource.getChildren()) {
					if (child == null) {
						continue;
					} else if (child instanceof MetaPrimaryKey) {
						continue;
					} else if (((MetaResourceField) child).isPrimaryKeyAttribute()) {
						continue;
					} else if (child instanceof MetaResourceField) {
						MetaResourceField mrf = (MetaResourceField) child;
						Schema attributeSchema = transformMetaResourceField(mrf.getType());
						attributeSchema.nullable(mrf.isNullable());
						attributes.put(mrf.getName(), attributeSchema);
						if (mrf.isAssociation()) {
							MetaResource relatedMetaResource = (MetaResource) mrf.getType().getElementType();
							PathItem relationPathItem = openApi.getPaths().getOrDefault(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + getApiPath(relatedMetaResource), new PathItem());
							if (mrf.isReadable()) {
								Operation getRelationshipOperation = mrf.getType().isCollection() || mrf.getType().isMap()
										? generateDefaultGetRelationshipsOperation(metaResource, relatedMetaResource)
										: generateDefaultGetRelationshipOperation(metaResource, relatedMetaResource);
								getRelationshipOperation.setDescription("Retrieve " + relatedMetaResource.getResourceType() + " related to a " + metaResource.getResourceType() + " resource");
								relationPathItem.setGet(mergeOperations(getRelationshipOperation, relationPathItem.getGet()));
								ApiResponses getRelationshipResponses = generateDefaultResponses(relatedMetaResource);
								String responsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "ListResponse" : "Response";
								getRelationshipResponses.addApiResponse("200", new ApiResponse().$ref(relatedMetaResource.getName() + responsePostFix));
								getRelationshipOperation.setResponses(getRelationshipResponses);
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + getApiPath(relatedMetaResource), relationPathItem);
							}
							if (mrf.isInsertable()) {
								Operation postRelationshipOperation = generateDefaultRelationshipOperation(metaResource, relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap(), true);
								postRelationshipOperation.setDescription("Create " + metaResource.getResourceType() + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
								relationPathItem.setPost(mergeOperations(postRelationshipOperation, relationPathItem.getPost()));
								ApiResponses postRelationshipResponses = generateDefaultResponses(relatedMetaResource);
								String responsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
								postRelationshipResponses.addApiResponse("200", new ApiResponse().$ref(relatedMetaResource.getName() + responsePostFix + "Response"));
								postRelationshipOperation.setResponses(postRelationshipResponses);
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + getApiPath(relatedMetaResource), relationPathItem);

							}
							if (mrf.isUpdatable()) {
								Operation patchRelationshipOperation = generateDefaultRelationshipOperation(metaResource, relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap(), true);
								patchRelationshipOperation.setDescription("Update " + metaResource.getResourceType() + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
								relationPathItem.setPatch(mergeOperations(patchRelationshipOperation, relationPathItem.getPatch()));
								ApiResponses patchRelationshipResponses = generateDefaultResponses(relatedMetaResource);
								String responsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
								patchRelationshipResponses.addApiResponse("200", new ApiResponse().$ref(relatedMetaResource.getName() + responsePostFix + "Response"));
								patchRelationshipOperation.setResponses(patchRelationshipResponses);
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + getApiPath(relatedMetaResource), relationPathItem);


								// If the relationship is updatable then we imply that it is deletable.

								// TODO: OpenAPI does not allow DELETE methods to define a RequestBody (https://github.com/OAI/OpenAPI-Specification/issues/1801)
								Operation deleteRelationshipOperation = generateDefaultRelationshipOperation(metaResource, relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap(), false);
								deleteRelationshipOperation.setDescription("Delete " + metaResource.getResourceType() + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
								relationPathItem.setDelete(mergeOperations(deleteRelationshipOperation, relationPathItem.getDelete()));
								ApiResponses deleteRelationshipResponses = generateDefaultResponses(relatedMetaResource);
//								String responsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
								deleteRelationshipResponses.addApiResponse("200", new ApiResponse().$ref(relatedMetaResource.getName() + responsePostFix + "Response"));
								deleteRelationshipOperation.setResponses(deleteRelationshipResponses);
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + getApiPath(relatedMetaResource), relationPathItem);
							}
						}
					}
				}

			}

			// TODO: Add Support for Bulk Operations
			if (metaResource.isInsertable()) {
				// List Response
				Operation operation = generateDefaultPostListOperation(metaResource);
				operation.setDescription("Create a " + metaResource.getName());
				listPathItem.setPost(mergeOperations(operation, listPathItem.getPost()));
				openApi.getPaths().addPathItem(getApiPath(metaResource), listPathItem);

				operation.setResponses(generateDefaultResponses(metaResource));
				operation.getResponses().addApiResponse("201", new ApiResponse().
						description("Created")
						.content(new Content()
								.addMediaType("application/json",
										new MediaType().schema(new Schema()
												.$ref(metaResource.getName() + "Response"))))
				);

				operation.getResponses().addApiResponse("202", new ApiResponse()
						.$ref("AcceptedResponse"));

				operation.getResponses().addApiResponse("204", new ApiResponse()
						.description("No Content"));

			}

			// TODO: Add Support for Bulk Operations
			if (metaResource.isUpdatable()) {
				// Single Response
				Operation operation = generateDefaultPatchSingleOperation(metaResource);
				operation.setDescription("Update a " + metaResource.getName());
				singlePathItem.setPatch(mergeOperations(operation, singlePathItem.getPatch()));
				openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource), singlePathItem);

				operation.setResponses(generateDefaultResponses(metaResource));
				operation.getResponses().addApiResponse("200", new ApiResponse().
						description("Ok")
						.content(new Content()
								.addMediaType("application/json",
										new MediaType().schema(new Schema()
												.$ref(metaResource.getName() + "Response"))))
				);

				operation.getResponses().addApiResponse("202", new ApiResponse()
						.$ref("AcceptedResponse"));

				operation.getResponses().addApiResponse("204", new ApiResponse()
						.description("No Content"));
			}

			// TODO: Add Support for Bulk Operations
			if (metaResource.isDeletable()) {
				// Single Response
				Operation operation = generateDefaultDeleteSingleOperation(metaResource);
				operation.setDescription("Delete a " + metaResource.getName());
				singlePathItem.setDelete(mergeOperations(operation, singlePathItem.getDelete()));
				openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource), singlePathItem);

				operation.setResponses(generateDefaultResponses(metaResource));
				operation.getResponses().addApiResponse("204", new ApiResponse()
						.description("The resource was deleted successfully"));
			}
		}

		write("openapi", Yaml.pretty(openApi));
	}

	/*
		Generate default schemas that are common across the api.
		For example, in JSON:API, the error response is common across all APIs
	 */
	private Map<String, Schema> generateDefaultSchemas() {
		Map<String, Schema> schemas = new LinkedHashMap<>();

		// Standard "Accepted" job response schema
		schemas.put("Accepted", new Schema()
				.type("object")
				.addProperties(
						"id",
						new Schema()
								.type("string")
								.description("a unique identifier for this pending action"))
		);

		// Standard Error Schema
		schemas.put("ApiError", jsonApiError());

		// Standard wrapper responses for single & multiple records
		schemas.put("ResponseMixin", responseMixin());
		schemas.put("ListResponseMixin", listResponseMixin());

		return schemas;
	}

	private Parameter generateDefaultFieldsParameter(MetaResource metaResource) {
		return new Parameter()
				.name("fields[" + metaResource.getResourceType() + "]")
				.description(metaResource.getResourceType() + " fields to include (csv)")
				.in("query")
				.schema(new StringSchema()
						._default(
								metaResource
										.getAttributes()
										.stream()
										.map(e -> ((MetaAttribute) e).getName())
										.collect(joining(","))));
	}

	private Parameter generateDefaultIncludeParameter(MetaResource metaResource) {
			return new Parameter()
					.name("include")
					.description(metaResource.getResourceType() + " relationships to include (csv)")
					.in("query")
					.schema(new StringSchema()
							._default(
									metaResource
											.getAttributes()
											.stream()
											.filter(e -> ((MetaAttribute) e).isAssociation())
											.map(e -> ((MetaAttribute) e).getType().getElementType().getName())
											.collect(joining(","))));
		}

	private Parameter generateDefaultSortParameter(MetaResource metaResource) {
		return new Parameter()
				.name("sort")
				.description(metaResource.getResourceType() + " sort order (csv)")
				.in("query")
				.schema(new StringSchema()
						.example(
								metaResource
										.getAttributes()
										.stream()
										.filter(e -> (((MetaAttribute) e).isSortable()))
										.map(e -> ((MetaAttribute) e).getName())
										.collect(joining(","))));
	}

	private Operation generateDefaultGetSingleOperation(MetaResource metaResource) {
		Operation operation = generateDefaultOperation();
		operation.getParameters().add(getPrimaryKeyParameter(metaResource));
		// Add fields[resource] parameter
		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + metaResource.getResourceType() + "Fields"));
		// Add include parameter
		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + metaResource.getResourceType() + "Include"));

		return operation;
	}

	private Parameter getPrimaryKeyParameter(MetaResource metaResource) {
		Parameter parameter = new Parameter();
		for (MetaElement metaElement : metaResource.getChildren()) {
			if (metaElement instanceof MetaAttribute) {
				MetaAttribute metaAttribute = (MetaAttribute) metaElement;
				if (metaAttribute.isPrimaryKeyAttribute()) {
					parameter = parameter
							.name(metaElement.getName())
							.in("path")
							.schema(transformMetaResourceField(((MetaAttribute) metaElement).getType()));
				}
			}
		}
		return parameter;
	}

	private Operation generateDefaultGetRelationshipsOperation(MetaResource metaResource, MetaResource relatedMetaResource) {
		Operation operation = generateDefaultOperation();
		operation.getParameters().add(getPrimaryKeyParameter(metaResource));

		// TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
		// Add filter[<>] parameters
		// Only the most basic filters are documented
		for (MetaElement child : relatedMetaResource.getChildren()) {
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
		// Add fields[resource] parameter
		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + relatedMetaResource.getResourceType() + "Fields"));
		// Add include parameter
		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + relatedMetaResource.getResourceType() + "Include"));

		return operation;
	}

	private Operation generateDefaultGetRelationshipOperation(MetaResource metaResource, MetaResource relatedMetaResource) {
		Operation operation = generateDefaultOperation();
		operation.getParameters().add(getPrimaryKeyParameter(metaResource));
		// Add fields[resource] parameter
		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + relatedMetaResource.getResourceType() + "Fields"));
		// Add include parameter
		operation.getParameters().add(new Parameter().$ref("#/components/parameters/" + relatedMetaResource.getResourceType() + "Include"));

		return operation;
	}

	private Operation generateDefaultRelationshipOperation(MetaResource metaResource, MetaResource relatedMetaResource, boolean oneToMany, boolean includeBody) {
		Operation operation = generateDefaultOperation();
		operation.getParameters().add(getPrimaryKeyParameter(metaResource));
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

	private Operation generateDefaultDeleteSingleOperation(MetaResource metaResource) {
		Operation operation = generateDefaultOperation();
		operation.getParameters().add(getPrimaryKeyParameter(metaResource));
		return operation;
	}

	private Operation generateDefaultPatchSingleOperation(MetaResource metaResource) {
		Operation operation = generateDefaultOperation();
		operation.getParameters().add(getPrimaryKeyParameter(metaResource));
		operation.setRequestBody(
				new RequestBody()
						.content(
								new Content()
										.addMediaType(
												"application/json",
												new MediaType()
														.schema(
																new Schema()
																		.$ref(metaResource.getName() + "Patch")))));
		return operation;
	}


	private Operation generateDefaultGetListOperation(MetaResource metaResource) {
		Operation operation = generateDefaultOperation();

		// TODO: Pull these out into re-usable parameter groups when https://github.com/OAI/OpenAPI-Specification/issues/445 lands
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

	private Operation generateDefaultPostListOperation(MetaResource metaResource) {
		Operation operation = generateDefaultOperation();
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

	private Operation generateDefaultOperation() {
		return new Operation().parameters(generateDefaultParameters());
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

	private List<Parameter> generateDefaultParameters() {
		return new ArrayList<Parameter>(
				Arrays.asList(
						new Parameter()
								.name("Content-Type")
								.in("header")
								.schema(generateContentTypeSchema())
								.required(true)
				));
	}

	private Schema generateContentTypeSchema() {
		StringSchema schema = new StringSchema();
		schema.setDefault("application/vnd.api+json");
		schema.setEnum(Arrays.asList("application/vnd.api+json", "application/json"));
		return schema;
	}

	/*
		Generate a sensible, default ApiResponses that is populated with references
		to all Error Responses for a metaResource
	 */
	private ApiResponses generateDefaultResponses(MetaResource metaResource) {
		ApiResponses responses = new ApiResponses();

		Map<String, ApiResponse> apiResponseCodes = getStandardApiErrorResponses();
		for (Map.Entry<String, ApiResponse> entry : apiResponseCodes.entrySet()) {

			// TODO: Check to see (somehow) if the metaResource returns this response code
			// Add reference to error response stored in #/components/responses/<HttpCode>
			responses.addApiResponse(entry.getKey(), new ApiResponse().$ref(entry.getKey()));
		}

		// Todo: Standard wrapper responses for single & multiple records
		// responses...

		return responses;
	}

	private ApiResponse getRelationshipResponse(String name) {
		return new ApiResponse()
				.description(HttpStatus.toMessage(200))
				.content(
						new Content()
								.addMediaType(
										"application/json",
										new MediaType()
												.schema(
														new Schema()
																.$ref(name + "Relationship"))));
	}

	private ApiResponse getRelationshipsResponse(String name) {
		return new ApiResponse()
				.description(HttpStatus.toMessage(200))
				.content(
						new Content()
								.addMediaType(
										"application/json",
										new MediaType()
												.schema(
														new Schema()
																.$ref(name + "Relationships"))));
	}

	private ApiResponse getSingleResponse(String name) {
		return new ApiResponse()
				.description(HttpStatus.toMessage(200))
				.content(
						new Content()
								.addMediaType(
										"application/json",
										new MediaType()
												.schema(
														new Schema()
																.$ref(name + "Response"))));
	}

	private ApiResponse getListResponse(String name) {
		return new ApiResponse()
				.description(HttpStatus.toMessage(200))
				.content(
						new Content()
								.addMediaType(
										"application/json",
										new MediaType()
												.schema(
														new Schema()
																.$ref(name + "ListResponse"))));
	}


	/*
		Using Crnks list of HTTP status codes, generate standard responses
		for all statuses in the error range. These ApiResponses will be shared
		across all endpoints.

		See "Reusing Responses" https://swagger.io/docs/specification/describing-responses/
	 */
	private Map<String, ApiResponse> getStandardApiErrorResponses() {
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

	private Map<String, Parameter> getStandardPagingParameters() {
		boolean NumberSizePagingBehavior = false;
		Map<String, Parameter> parameters = new LinkedHashMap<>();
		parameters.put(
				"pageLimit",
				new Parameter().name("page[limit]")
						.description("Max number of items")
						.in("query")
						.schema(
								new IntegerSchema()
										.format("int64")
										._default(100)  // TODO: resolve from application.properties.crnk.default-page-limit=20
										.maximum(BigDecimal.valueOf(1000))));  // TODO: resolve from application.properties.crnk.max-page-limit=1000
		parameters.put(
				"pageOffset",
				new Parameter().name("page[offset]")
						.description("Page offset")
						.in("query")
						.schema(
								new IntegerSchema()
										.format("int64")
										._default(0)));

		if (NumberSizePagingBehavior) {  // TODO: Figure out how to determine this
			parameters.put(
					"pageNumber",
					new Parameter().name("page[number]")
							.description("Page number")
							.in("query")
							.schema(
									new IntegerSchema()
											.format("int64")
											._default(1)));

			parameters.put(
					"pageSize",
					new Parameter().name("page[size]")
							.description("Page size")
							.in("query")
							.schema(
									new IntegerSchema()
											.format("int64")
											._default(0)));  // TODO: resolve from application.properties.crnk.default-page-limit=20
		}

		return parameters;
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

	private String getPrimaryKeyPath(MetaResource metaResource) {
		StringBuilder keyPath = new StringBuilder("/");
		for (MetaAttribute metaAttribute : metaResource.getPrimaryKey().getElements()) {
			keyPath.append("{");
			keyPath.append(metaAttribute.getName());
			keyPath.append("}");
		}
		return keyPath.toString();
	}

	private String getApiPath(MetaResource metaResource) {
		//
		// TODO: Requires access to CrnkBoot.getWebPathPrefix() and anything that might modify a path
		// TODO: alternatively, have a config setting for this generator that essentially duplicates the above
		//
		return "/" + metaResource.getResourcePath();
	}

	private Schema transformMetaResourceField(MetaType metaType) {
		if (metaType instanceof MetaResource) {
			return get$refSchema(((MetaResource) metaType).getResourceType() + "Reference");
		} else if (metaType instanceof MetaCollectionType) {
			return new ArraySchema()
					.items(transformMetaResourceField(metaType.getElementType()))
					.uniqueItems(metaType instanceof MetaSetType);
		} else if (metaType instanceof MetaArrayType) {
			return new ArraySchema()
					.items(transformMetaResourceField(metaType.getElementType()))
					.uniqueItems(false);
		} else if (metaType instanceof MetaJsonObject) {
			ObjectSchema objectSchema = new ObjectSchema();
			for (MetaElement child : metaType.getChildren()) {
				if (child instanceof MetaAttribute) {
					MetaAttribute metaAttribute = (MetaAttribute) child;
					objectSchema.addProperties(child.getName(), transformMetaResourceField(metaAttribute.getType()));
				}
			}
			return objectSchema;
		} else if (metaType.getName().equals("boolean")) {
			return new BooleanSchema();
		} else if (metaType.getName().equals("byte")) {
			return new ByteArraySchema();
		} else if (metaType.getName().equals("date")) {
			return new DateSchema();
		}
		// TODO: Exhaustively enumerate Date formats, or find another way to check
		else if (metaType.getName().equals("offsetDateTime")) {
			return new DateTimeSchema();
		} else if (metaType.getName().equals("double")) {
			return new NumberSchema().format("double");
		} else if (metaType.getName().equals("float")) {
			return new NumberSchema().format("float");
		} else if (metaType.getName().equals("integer")) {
			return new IntegerSchema().format("int32");
		} else if (metaType.getName().equals("json")) {
			return new ObjectSchema();
		} else if (metaType.getName().equals("json.object")) {
			return new ObjectSchema();
		} else if (metaType.getName().equals("json.array")) {
			return new ArraySchema().items(new Schema());
		} else if (metaType.getName().equals("long")) {
			return new IntegerSchema().format("int64");
		} else if (metaType.getName().equals("object")) {
			return new ObjectSchema();
		} else if (metaType.getName().equals("short")) {
			return new IntegerSchema().minimum(BigDecimal.valueOf(Short.MIN_VALUE)).maximum(BigDecimal.valueOf(Short.MAX_VALUE));
		} else if (metaType.getName().equals("string")) {
			return new StringSchema();
		} else if (metaType.getName().equals("uuid")) {
			return new UUIDSchema();
		} else if (metaType instanceof MetaMapType) {

			return transformMetaResourceField(metaType.getElementType());
		} else if (metaType instanceof MetaEnumType) {
			Schema enumSchema = new StringSchema();
			for (MetaElement child : metaType.getChildren()) {
				if (child instanceof MetaLiteral) {
					enumSchema.addEnumItemObject(child.getName());
				} else {
					return new ObjectSchema();
				}
			}
			return enumSchema;
		} else {
			Schema schema = new Schema().type(metaType.getElementType().getName());
			return schema;
		}
	}

	private Schema listResponseMixin() {
		return new Schema()
				.type("object")
				.description("A page of results")
				.addProperties(
						"jsonapi",
						new Schema()
								.type("object")
								.addProperties(
										"version",
										new Schema().type("string")))
				.addProperties(
						"errors",
						new ArraySchema().items(new Schema().$ref("ApiError")))
				.addProperties(
						"meta",
						new Schema()
								.type("object")
// TODO: Determine if this is supported
//								.addProperties(
//										"total-pages",
//										new Schema()
//												.type("integer")
//												.description("The total number of pages available"))
								.addProperties(
										"totalResourceCount",
										new Schema()
												.type("integer")
												.description("The total number of items available"))
								.additionalProperties(true))
				.addProperties(
						"links",
						new Schema()
								.type("object")
								.addProperties(
										"self",
										new Schema()
												.type("string")
												.description("Link to this page of results"))
								.addProperties(
										"prev",
										new Schema()
												.type("string")
												.description("Link to the previous page of results"))
								.addProperties(
										"next",
										new Schema()
												.type("string")
												.description("Link to the next page of results"))
								.addProperties(
										"last",
										new Schema()
												.type("string")
												.description("Link to the last page of results"))
								.addProperties(
										"first",
										new Schema()
												.type("string")
												.description("Link to the first page of results")));
	}

	private Schema jsonApiError() {
		return new Schema()
				.type("object")
				.addProperties(
						"id",
						new Schema()
								.type("string")
								.description("a unique identifier for this particular occurrence of the problem"))
				.addProperties("links",
						new Schema()
								.type("object")
								.addProperties(
										"about",
										new Schema()
												.type("string")
												.description("a link that leads to further details about this particular occurrence of the problem")))
				.addProperties(
						"status",
						new Schema()
								.type("string")
								.description("the HTTP status code applicable to this problem, expressed as a string value"))
				.addProperties(
						"code",
						new Schema()
								.type("string")
								.description("an application-specific error code, expressed as a string value"))
				.addProperties(
						"title",
						new Schema()
								.type("string")
								.description("a short, human-readable summary of the problem that SHOULD NOT change from occurrence to occurrence of the problem, except for purposes of localization"))
				.addProperties(
						"detail",
						new Schema()
								.type("string")
								.description("a human-readable explanation specific to this occurrence of the problem. Like 'title', this field’s value can be localized."))
				.addProperties(
						"source",
						new Schema()
								.type("object")
								.addProperties(
										"pointer",
										new Schema()
												.type("string")
												.description("a JSON Pointer [RFC6901] to the associated entity in the request document"))
								.addProperties(
										"parameter",
										new Schema()
												.type("string")
												.description("a string indicating which URI query parameter caused the error")))
				.addProperties(
						"meta",
						new Schema()
								.additionalProperties(true)
								.description("a meta object containing non-standard meta-information about the error"));
	}

	private Schema get$refSchema(String typeName) {
		return new Schema().$ref("#/components/schemas/" + typeName);
	}

	private Schema responseMixin() {
		return new Schema()
				.type("object")
				.description("A JSON-API document with a single resource")
				.addProperties(
						"errors",
						new ArraySchema().items(new Schema().$ref("ApiError")))
				.addProperties(
						"jsonapi",
						new Schema()
								.type("object")
								.addProperties(
										"version",
										new Schema().type("string")))
				.addProperties(
						"links",
						new Schema().addProperties(
								"self",
								new Schema()
										.type("string")
										.description("the link that generated the current response document")))
				.addProperties(
						"included",
						new ArraySchema()
								.items(
										new Schema()
												.type("object")
												.addProperties(
														"type",
														new Schema()
																.type("string")
																.description("The JSON:API resource type"))
												.addProperties(
														"id",
														new Schema()
																.type("string")
																.description("The JSON:API resource ID")))
								.description("Included resources"));
	}

	private Schema getTypeSchema(String typeName) {
		Schema typeSchema = new Schema()
				.type("string")
				.description("The JSON:API resource type (" + typeName + ")");
		typeSchema.setEnum(Arrays.asList(typeName));
		return typeSchema;
	}

	private Schema resourceResponse(String typeName) {
		return new ComposedSchema()
				.allOf(
						Arrays.asList(
								get$refSchema("ResponseMixin"),
								new Schema()
										.addProperties(
												"data",
												new ArraySchema()
														.items(
																get$refSchema(typeName)))
										.required(Arrays.asList("data"))));
	}

	private Schema resourceListResponse(String typeName) {
		return new ComposedSchema()
				.allOf(
						Arrays.asList(
								get$refSchema("ListResponseMixin"),
								new Schema()
										.addProperties(
												"data",
												new ArraySchema()
														.items(
																get$refSchema(typeName)))
										.required(Arrays.asList("data"))));
	}

	private Schema resourceReference(String typeName) {
		return new Schema()
				.type("object")
				.addProperties(
						"type",
						getTypeSchema(typeName))
				.addProperties(
						"id",
						new Schema()
								.type("string")
								.description("The JSON:API resource ID"))
				.required(Arrays.asList("id", "type"));
	}

	private Schema resource(String resourceType, Map<String, Schema> attributes) {
		//Defines a schema for a JSON-API resource, without the enclosing top-level document.
		return new ComposedSchema()
				.allOf(
						Arrays.asList(
								get$refSchema(resourceType + "Reference"),
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
										.required(Arrays.asList("attributes"))));
	}

	private Schema resourceRequestBody(String resourceType, Map<String, Schema> attributes) {
		//Defines a schema for the PATCH parameters of a JSON:API resource
		return new ComposedSchema()
				.allOf(
						Arrays.asList(
								get$refSchema(resourceType + "Reference"),
								new Schema()
										.type("object")
										.addProperties(
												"attributes",
												new Schema()
														.type("object")
														.properties(attributes))));
	}

	private Schema singleRelationshipBody(String resourceType) {
		//Defines a schema for the PATCH parameters of a JSON:API resource
		return new ObjectSchema()
				.addProperties(
						"data",
						get$refSchema(resourceType + "Reference"));
	}

	private Schema multiRelationshipBody(String resourceType) {
		//Defines a schema for the PATCH parameters of a JSON:API resource
		return new ObjectSchema()
				.addProperties(
						"data",
						new ArraySchema()
								.items(get$refSchema(resourceType + "Reference")));
	}		

	private Schema hasOneRelationshipData(String name) {
		return new Schema()
				.type("object")
				.addProperties(
						"id",
						new Schema()
								.type("string")
								.description("Related " + name + " resource id"))
				.addProperties(
						"type",
						new Schema()
								.type("string")
								.description("Type of related " + name + " resource"));
	}

	private ArraySchema hasManyRelationshipData(String name) {
		return (new ArraySchema())
				.items(hasOneRelationshipData(name));
	}

	private Schema getRelationshipSchema(String name, String relationshipType) {
		if (relationshipType.equals("hasOne")) {
			return hasOneRelationshipData(name);
		} else if (relationshipType.equals("hasMany")) {
			return hasManyRelationshipData(name);
		}
		return null;
	}

	private Schema relationship(String name, String relationshipType, boolean nullable) {
		Schema schema = new Schema()
				.type("object")
				.addProperties(
						"links",
						new Schema()
								.type("object")
								.addProperties(
										"self",
										new Schema()
												.type("string")
												.description("Relationship link for " + name))
								.addProperties(
										"related",
										new Schema()
												.type("object")
												.description("Related " + name + " link")
												.addProperties(
														"href",
														new Schema()
																.type("string"))
												.addProperties(
														"meta",
														new Schema()
																.type("object")
																.additionalProperties(true))));
		if (nullable) {
			return schema;
		}
		return schema.addProperties(
				"data",
				getRelationshipSchema(name, relationshipType));
	}

	@Override
	public ClassLoader getClassLoader() {
		return classloader;
	}

	@Override
	public void setClassLoader(ClassLoader classloader) {
		this.classloader = classloader;
	}

	@Override
	public void initDefaults(File buildDir) {
		config.setBuildDir(buildDir);
	}

	@Override
	public File getGenDir() {
		return config.getGenDir();
	}

	@Override
	public Collection<Class> getConfigClasses() {
		return Arrays.asList(OpenAPIGeneratorConfig.class);
	}

	public OpenAPIGeneratorConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(GeneratorModuleConfigBase config) {
		this.config = (OpenAPIGeneratorConfig) config;
	}


	private File write(String fileName, String source) throws IOException {
		File file = new File(config.getGenDir(), fileName + ".yaml");
		file.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(source);
		}
		return file;
	}
}
