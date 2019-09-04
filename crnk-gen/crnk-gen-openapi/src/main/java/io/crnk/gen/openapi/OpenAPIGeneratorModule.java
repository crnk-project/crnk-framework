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
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;


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
		OpenAPI openApi = new OpenAPI()
				.info(new Info()
						.description("TEST INFO DESC") // TODO: Must be configurable
						.title("TEST INFO TITLE") // TODO: Must be configurable
						.version("0.1.0") // TODO: Must be configurable
				)
				.paths(new Paths());

		openApi.components(new Components());
		openApi.getComponents().schemas(generateDefaultSchemas());
		openApi.getComponents().responses(getStandardApiErrorResponses());

		MetaLookup metaLookup = (MetaLookup) meta;
		List<MetaResource> metaResources = getJsonApiResources(metaLookup);
		for (MetaResource metaResource : metaResources) {
			PathItem pathItem = new PathItem();
			pathItem.setDescription(metaResource.getName());

			// Create Component
			Map<String, Schema> attributes = new HashMap<>();
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
				}
			}

			// Add ReferenceType Schema
			Schema resourceReference = resourceReference(metaResource.getName());
			openApi.getComponents().addSchemas(metaResource.getResourceType() + "Reference", resourceReference);

			// Add Types Schema
			Schema resource = resource(metaResource.getResourceType(), attributes);
			openApi.getComponents().addSchemas(metaResource.getName(), resource);

			// Add Response Schema
			Schema resourceResponse = resourceResponse(metaResource.getName());
			openApi.getComponents().addSchemas(metaResource.getName() + "Response", resourceResponse);

			// Add ListResponse Schema
			Schema resourceListResponse = resourceListResponse(metaResource.getName());
			openApi.getComponents().addSchemas(metaResource.getName() + "ListResponse", resourceListResponse);

			if (metaResource.isInsertable()) {
				Operation operation = new Operation();
				operation.setOperationId("create" + metaResource.getName());
				operation.setDescription("Create a " + metaResource.getName());
				pathItem.setPost(operation);
				openApi.getPaths().addPathItem(getApiPath(metaResource), pathItem);

				operation.setResponses(generateDefaultResponses(metaResource));
			}

			if (metaResource.isReadable()) {
				Operation operation = new Operation();
				operation.setOperationId("get" + metaResource.getName());
				operation.setDescription("Read a " + metaResource.getName());
				pathItem.setGet(operation);
				openApi.getPaths().addPathItem(getApiPath(metaResource), pathItem);

				operation.setResponses(generateDefaultResponses(metaResource));
			}

			if (metaResource.isUpdatable()) {
				Operation operation = new Operation();
				operation.setOperationId("update" + metaResource.getName());
				operation.setDescription("Update a " + metaResource.getName());
				pathItem.setPatch(operation);
				openApi.getPaths().addPathItem(getApiPath(metaResource), pathItem);

				operation.setResponses(generateDefaultResponses(metaResource));
			}

			if (metaResource.isDeletable()) {
				Operation operation = new Operation();
				operation.setOperationId("delete" + metaResource.getName());
				operation.setDescription("Delete a " + metaResource.getName());
				pathItem.setDelete(operation);
				openApi.getPaths().addPathItem(getApiPath(metaResource), pathItem);

				operation.setResponses(generateDefaultResponses(metaResource));
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

		// Standard Error Schema
		schemas.put("ApiError", jsonApiError());

		// Standard wrapper responses for single & multiple records
		schemas.put("ResponseMixin", responseMixin());
		schemas.put("ListResponseMixin", listResponseMixin());

		return schemas;
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

	private String getApiPath(MetaResource metaResource) {
		//
		// TODO: Requires access to CrnkBoot.getWebPathPrefix() and anything that might modify a path
		// TODO: alternatively, have a config setting for this generator that essentially duplicates the above
		//
		return "/todo/" + metaResource.getResourcePath();
	}

	protected Schema transformMetaResourceField(MetaType metaType) {
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
		} else if (metaType.getName().equals("offsetDateTime")) {
			return new DateTimeSchema();
		} else if (metaType.getName().equals("double")) {
			return new NumberSchema();
		} else if (metaType.getName().equals("float")) {
			return new NumberSchema();
		} else if (metaType.getName().equals("integer")) {
			return new IntegerSchema();
		} else if (metaType.getName().equals("json")) {
			return new ObjectSchema();
		} else if (metaType.getName().equals("json.object")) {
			return new ObjectSchema();
		} else if (metaType.getName().equals("json.array")) {
			// return new ArraySchema().items("{}");
			// The desired value is
			// arrayNodeValue:
			//   type: array
			//   items: {}
			// But the OpenAPI SDK does not support it.
			return new ObjectSchema();
		} else if (metaType.getName().equals("long")) {
			return new NumberSchema();
		} else if (metaType.getName().equals("object")) {
			return new ObjectSchema();
		} else if (metaType.getName().equals("short")) {
			return new NumberSchema();
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

	protected Schema listResponseMixin() {
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
								.addProperties(
										"total-pages",
										new Schema()
												.type("integer")
												.description("The total number of pages available"))
								.addProperties(
										"total-count",
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

	protected Schema get$refSchema(String typeName) {
		return new Schema().$ref("#/components/schemas/" + typeName);
	}

	protected Schema responseMixin() {
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

	protected Schema getTypeSchema(String typeName) {
		Schema typeSchema = new Schema()
				.type("string")
				.description("The JSON:API resource type (" + typeName + ")");
		typeSchema.setEnum(Arrays.asList(typeName));
		return typeSchema;
	}

	protected Schema resourceResponse(String typeName) {
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

	protected Schema resourceListResponse(String typeName) {
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

	protected Schema resourceReference(String typeName) {
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

	protected Schema resource(String resourceType, Map<String, Schema> attributes) {
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

	protected Schema hasOneRelationshipData(String name) {
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

	protected ArraySchema hasManyRelationshipData(String name) {
		return (new ArraySchema())
				.items(hasOneRelationshipData(name));
	}

	protected Schema getRelationshipSchema(String name, String relationshipType) {
		if (relationshipType.equals("hasOne")) {
			return hasOneRelationshipData(name);
		} else if (relationshipType.equals("hasMany")) {
			return hasManyRelationshipData(name);
		}
		return null;
	}

	protected Schema relationship(String name, String relationshipType, boolean nullable) {
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


	protected String getTypeFromRef(String ref) {
		int lastSlash = ref.lastIndexOf("/");
		int lastHash = ref.lastIndexOf("#");
		return ref.substring(Math.max(lastSlash, lastHash) + 1);
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