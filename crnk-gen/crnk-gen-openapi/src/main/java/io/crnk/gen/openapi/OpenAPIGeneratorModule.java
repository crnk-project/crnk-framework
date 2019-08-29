package io.crnk.gen.openapi;

import io.crnk.core.engine.http.HttpStatus;
import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
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
		List<MetaResource> metaResourceList = getJsonApiResources(metaLookup);
		for (MetaResource metaResource : metaResourceList) {

			PathItem pathItem = new PathItem();
			pathItem.setDescription(metaResource.getImplementationClass().getSimpleName());

			if (metaResource.isInsertable()) {
				Operation operation = new Operation();
				operation.setOperationId("create" + metaResource.getImplementationClass().getSimpleName());
				operation.setDescription("Create a " + metaResource.getImplementationClass().getSimpleName());
				pathItem.setPost(operation);
				openApi.getPaths().addPathItem(getApiPath(metaResource), pathItem);

				operation.setResponses(generateDefaultResponses(metaResource));
			}

			if (metaResource.isReadable()) {
				Operation operation = new Operation();
				operation.setOperationId("get" + metaResource.getImplementationClass().getSimpleName());
				operation.setDescription("Read a " + metaResource.getImplementationClass().getSimpleName());
				pathItem.setGet(operation);
				openApi.getPaths().addPathItem(getApiPath(metaResource), pathItem);

				operation.setResponses(generateDefaultResponses(metaResource));
			}

			if (metaResource.isUpdatable()) {
				Operation operation = new Operation();
				operation.setOperationId("update" + metaResource.getImplementationClass().getSimpleName());
				operation.setDescription("Update a " + metaResource.getImplementationClass().getSimpleName());
				pathItem.setPatch(operation);
				openApi.getPaths().addPathItem(getApiPath(metaResource), pathItem);

				operation.setResponses(generateDefaultResponses(metaResource));
			}

			if (metaResource.isDeletable()) {
				Operation operation = new Operation();
				operation.setOperationId("delete" + metaResource.getImplementationClass().getSimpleName());
				operation.setDescription("Delete a " + metaResource.getImplementationClass().getSimpleName());
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

		// Todo: Standard wrapper responses for single & multiple records
		// schemas.put(...);

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
		//
		return "/todo/" + metaResource.getResourcePath();
	}

	protected Schema page(String resource) {
		String name = getTypeFromRef(resource);
		return new Schema()
				.type("object")
				.description("A page of " + name + " results")
				.addProperties(
						"jsonapi",
						new Schema()
								.type("object")
								.addProperties(
										"version",
										new Schema().type("string")))
				.addProperties(
						"errors",
						new ArraySchema().items(jsonApiError()))
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
												.description("Link to the first page of results")))
				.addProperties(
						"data",
						new ArraySchema()
								.items(new Schema()
										.$ref(resource))
								.description("Content with " + name + "objects"));
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

	protected Schema singleDocument(String name) {
		return new Schema()
				.type("object")
				.description("A JSON-API document with a single " + name + " resource")
				.addProperties(
						"errors",
						new ArraySchema().items(jsonApiError()))
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
						"data",
						new Schema().$ref("#/definitions/" + name))
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
								.description("Included resources"))
				.required(Arrays.asList("data"));
	}


	protected Schema jsonApiResource() {
		//Defines a schema for a JSON-API resource, without the enclosing top-level document.
		return new Schema()
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
								.description("The JSON:API resource ID"))
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
								.type("object"));
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


	private String getTypeFromRef(String ref) {
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