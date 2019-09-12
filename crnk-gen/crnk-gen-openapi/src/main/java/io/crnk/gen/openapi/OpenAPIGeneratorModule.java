package io.crnk.gen.openapi;

import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.gen.openapi.internal.OASGenerator;
import io.crnk.gen.openapi.internal.OASResource;
import io.crnk.gen.openapi.internal.OASUtils;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
		OpenAPI openApi = new OASGenerator(config.getOpenAPI()).getOpenApi();

		// TODO: Respect @JsonApiExposed(false)
		MetaLookup metaLookup = (MetaLookup) meta;
		List<MetaResource> metaResources = getJsonApiResources(metaLookup);
		for (MetaResource metaResource : metaResources) {
			PathItem listPathItem = openApi.getPaths().getOrDefault(getApiPath(metaResource), new PathItem());
			PathItem singlePathItem = openApi.getPaths().getOrDefault(getApiPath(metaResource) + getPrimaryKeyPath(metaResource), new PathItem());

      OASResource oasResource = new OASResource(metaResource);

			// Add Fields Parameter
			openApi.getComponents().addParameters(metaResource.getResourceType() + "Fields", oasResource.generateDefaultFieldsParameter());

			// Add Include Parameter
			openApi.getComponents().addParameters(metaResource.getResourceType() + "Include", oasResource.generateDefaultIncludeParameter());

			// Add Sort parameter
			openApi.getComponents().addParameters(metaResource.getResourceType() + "Sort", oasResource.generateDefaultSortParameter());

			// Add ReferenceType Schema
			openApi.getComponents().addSchemas(metaResource.getResourceType() + "Reference", oasResource.resourceReference());

			// Add Resource Schema
			openApi.getComponents().addSchemas(metaResource.getName(), oasResource.resource());

			// Add PATCH Resource Schema
			openApi.getComponents().addSchemas(metaResource.getName() + "Patch", oasResource.patchResourceRequestBody());

			// Add POST Resource Schema
			openApi.getComponents().addSchemas(metaResource.getName() + "Post", oasResource.postResourceRequestBody());

      // Add Response Schema
      openApi.getComponents().addSchemas(metaResource.getName() + "Response", oasResource.resourceResponse());


      // Add ListResponse Schema
      openApi.getComponents().addSchemas(metaResource.getName() + "ListResponse", oasResource.resourceListResponse());

			// Add relationship modification request body
			openApi.getComponents().addSchemas(metaResource.getName() + "Relationship", oasResource.singleRelationshipBody());

			// Add relationships modification request body
			openApi.getComponents().addSchemas(metaResource.getName() + "Relationships", oasResource.multiRelationshipBody());

      openApi.getComponents().addResponses(metaResource.getName() + "Response", oasResource.getSingleResponse());
      openApi.getComponents().addResponses(metaResource.getName() + "ListResponse", oasResource.getListResponse());
			openApi.getComponents().addResponses(metaResource.getName() + "RelationshipResponse", oasResource.getRelationshipResponse());
			openApi.getComponents().addResponses(metaResource.getName() + "RelationshipsResponse", oasResource.getRelationshipsResponse());

			// Relationships can be accessed in 2 ways:
			//  1.	/api/A/1/b  								The full related resource
			//  2.	/api/A/1/relationships/b		The "ids" as belong to the resource
			if (metaResource.isReadable()) {
				// List Response
				Operation getListOperation = oasResource.generateDefaultGetListOperation(metaResource);
        listPathItem.setGet(mergeOperations(getListOperation, listPathItem.getGet()));
        openApi.getPaths().addPathItem(getApiPath(metaResource), listPathItem);

				// Single Response
				Operation getSingleOperation = oasResource.generateDefaultGetSingleOperation(metaResource);
        singlePathItem.setGet(mergeOperations(getSingleOperation, singlePathItem.getGet()));
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
						Schema attributeSchema = OASUtils.transformMetaResourceField(mrf.getType());
						attributeSchema.nullable(mrf.isNullable());
						oasResource.getAttributes().put(mrf.getName(), attributeSchema);
						if (mrf.isAssociation()) {
							MetaResource relatedMetaResource = (MetaResource) mrf.getType().getElementType();
							PathItem fieldPathItem = openApi.getPaths().getOrDefault(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + getApiPath(relatedMetaResource), new PathItem());
							PathItem relationshipPathItem = openApi.getPaths().getOrDefault(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + "/relationships" + getApiPath(relatedMetaResource), new PathItem());

							if (mrf.isReadable()) {
								Operation getFieldOperation = oasResource.generateDefaultGetRelationshipsOperation(relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap());
								getFieldOperation.setDescription("Retrieve " + relatedMetaResource.getResourceType() + " related to a " + metaResource.getResourceType() + " resource");
								Map<String, ApiResponse> getFieldResponses = oasResource.generateDefaultResponsesMap();
								String responsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "ListResponse" : "Response";
								getFieldResponses.put("200", new ApiResponse().$ref(relatedMetaResource.getName() + responsePostFix));
								getFieldOperation.setResponses(OASUtils.apiResponsesFromMap(getFieldResponses));
                fieldPathItem.setGet(mergeOperations(getFieldOperation, fieldPathItem.getGet()));
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + getApiPath(relatedMetaResource), fieldPathItem);

								// Add relationships/ path
								Operation getRelationshipOperation = oasResource.generateDefaultGetRelationshipsOperation(relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap());
								getRelationshipOperation.setDescription("Retrieve " + relatedMetaResource.getResourceType() + " references related to a " + metaResource.getResourceType() + " resource");
								Map<String, ApiResponse> getRelationshipResponses = oasResource.generateDefaultResponsesMap();
								String sparseResponsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "RelationshipsResponse" : "RelationshipResponse";
								getRelationshipResponses.put("200", new ApiResponse().$ref(relatedMetaResource.getName() + sparseResponsePostFix));
								getRelationshipOperation.setResponses(OASUtils.apiResponsesFromMap(getRelationshipResponses));
                relationshipPathItem.setGet(mergeOperations(getRelationshipOperation, relationshipPathItem.getGet()));
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + "/relationships" + getApiPath(relatedMetaResource), relationshipPathItem);
							}
							if (mrf.isInsertable()) {
								Operation postFieldOperation = oasResource.generateDefaultRelationshipOperation(relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap(), true);
								postFieldOperation.setDescription("Create " + metaResource.getResourceType() + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
								Map<String, ApiResponse> postFieldResponses = oasResource.generateDefaultResponsesMap();
								String responsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
								postFieldResponses.put("200", new ApiResponse().$ref(relatedMetaResource.getName() + responsePostFix + "Response"));
								postFieldOperation.setResponses(OASUtils.apiResponsesFromMap(postFieldResponses));
                fieldPathItem.setPost(mergeOperations(postFieldOperation, fieldPathItem.getPost()));
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + getApiPath(relatedMetaResource), fieldPathItem);

								// Add relationships/ path
								Operation postRelationshipOperation = oasResource.generateDefaultRelationshipOperation(relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap(), true);
								postRelationshipOperation.setDescription("Create " + metaResource.getResourceType() + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
								Map<String, ApiResponse> postRelationshipResponses = oasResource.generateDefaultResponsesMap();
								String sparseResponsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
								postRelationshipResponses.put("200", new ApiResponse().$ref(relatedMetaResource.getName() + sparseResponsePostFix + "Response"));
								postRelationshipOperation.setResponses(OASUtils.apiResponsesFromMap(postRelationshipResponses));
                relationshipPathItem.setPost(mergeOperations(postRelationshipOperation, relationshipPathItem.getPost()));
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + "/relationships" + getApiPath(relatedMetaResource), relationshipPathItem);
							}
							if (mrf.isUpdatable()) {
								Operation patchFieldOperation = oasResource.generateDefaultRelationshipOperation(relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap(), true);
								patchFieldOperation.setDescription("Update " + metaResource.getResourceType() + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
								Map<String, ApiResponse> patchFieldResponses = oasResource.generateDefaultResponsesMap();
								String responsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
								patchFieldResponses.put("200", new ApiResponse().$ref(relatedMetaResource.getName() + responsePostFix + "Response"));
								patchFieldOperation.setResponses(OASUtils.apiResponsesFromMap(patchFieldResponses));
                fieldPathItem.setPatch(mergeOperations(patchFieldOperation, fieldPathItem.getPatch()));
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + getApiPath(relatedMetaResource), fieldPathItem);

								// Add relationships/ path
								Operation patchRelationshipOperation = oasResource.generateDefaultRelationshipOperation(relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap(), true);
								patchRelationshipOperation.setDescription("Update " + metaResource.getResourceType() + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
								Map<String, ApiResponse> patchRelationshipResponses = oasResource.generateDefaultResponsesMap();
								String sparseResponsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
								patchRelationshipResponses.put("200", new ApiResponse().$ref(relatedMetaResource.getName() + sparseResponsePostFix + "Response"));
								patchRelationshipOperation.setResponses(OASUtils.apiResponsesFromMap(patchRelationshipResponses));
                relationshipPathItem.setPatch(mergeOperations(patchRelationshipOperation, relationshipPathItem.getPatch()));
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + "/relationships" + getApiPath(relatedMetaResource), relationshipPathItem);

								// If the relationship is updatable then we imply that it is deletable.

								// TODO: OpenAPI does not allow DELETE methods to define a RequestBody (https://github.com/OAI/OpenAPI-Specification/issues/1801)
								Operation deleteFieldOperation = oasResource.generateDefaultRelationshipOperation(relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap(), false);
								deleteFieldOperation.setDescription("Delete " + metaResource.getResourceType() + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
								Map<String, ApiResponse> deleteFieldResponses = oasResource.generateDefaultResponsesMap();
//								String responsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
								deleteFieldResponses.put("200", new ApiResponse().$ref(relatedMetaResource.getName() + responsePostFix + "Response"));
								deleteFieldOperation.setResponses(OASUtils.apiResponsesFromMap(deleteFieldResponses));
                fieldPathItem.setDelete(mergeOperations(deleteFieldOperation, fieldPathItem.getDelete()));
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + getApiPath(relatedMetaResource), fieldPathItem);

								Operation deleteRelationshipOperation = oasResource.generateDefaultRelationshipOperation(relatedMetaResource, mrf.getType().isCollection() || mrf.getType().isMap(), false);
								deleteRelationshipOperation.setDescription("Delete " + metaResource.getResourceType() + " relationship to a " + relatedMetaResource.getResourceType() + " resource");
								Map<String, ApiResponse> deleteRelationshipResponses = oasResource.generateDefaultResponsesMap();
//								String responsePostFix = mrf.getType().isCollection() || mrf.getType().isMap() ? "Relationships" : "Relationship";
								deleteRelationshipResponses.put("200", new ApiResponse().$ref(relatedMetaResource.getName() + responsePostFix + "Response"));
								deleteRelationshipOperation.setResponses(OASUtils.apiResponsesFromMap(deleteRelationshipResponses));
                relationshipPathItem.setDelete(mergeOperations(deleteRelationshipOperation, relationshipPathItem.getDelete()));
								openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource) + "/relationships" + getApiPath(relatedMetaResource), relationshipPathItem);
							}
						}
					}
				}
			}

			// TODO: Add Support for Bulk Operations
			if (metaResource.isInsertable()) {
				// List Response
				Operation operation = oasResource.generateDefaultPostListOperation();
				listPathItem.setPost(mergeOperations(operation, listPathItem.getPost()));
				openApi.getPaths().addPathItem(getApiPath(metaResource), listPathItem);
			}

			// TODO: Add Support for Bulk Operations
			if (metaResource.isUpdatable()) {
				// Single Response
				Operation operation = oasResource.generateDefaultPatchSingleOperation();
				singlePathItem.setPatch(mergeOperations(operation, singlePathItem.getPatch()));
				openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource), singlePathItem);

			}

			// TODO: Add Support for Bulk Operations
			if (metaResource.isDeletable()) {
				// Single Response
				Operation operation = oasResource.generateDefaultDeleteSingleOperation();
				singlePathItem.setDelete(mergeOperations(operation, singlePathItem.getDelete()));
				openApi.getPaths().addPathItem(getApiPath(metaResource) + getPrimaryKeyPath(metaResource), singlePathItem);
			}
		}

		write("openapi", Yaml.pretty(openApi));
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



	private Schema singleRelationshipBody(String resourceType) {
		//Defines a schema for the PATCH parameters of a JSON:API resource
		return new ObjectSchema()
				.addProperties(
						"data",
            OASUtils.get$refSchema(resourceType + "Reference"));
	}

	private Schema multiRelationshipBody(String resourceType) {
		//Defines a schema for the PATCH parameters of a JSON:API resource
		return new ObjectSchema()
				.addProperties(
						"data",
						new ArraySchema()
								.items(OASUtils.get$refSchema(resourceType + "Reference")));
	}

	private Schema hasOneRelationshipData(MetaResource metaResource) {
		return OASUtils.get$refSchema(metaResource.getResourceType() + "Reference");
	}

	private ArraySchema hasManyRelationshipData(MetaResource metaResource) {
		return (new ArraySchema())
				.items(hasOneRelationshipData(metaResource));
	}

	private Schema getRelationshipSchema(MetaResource metaResource, boolean oneToMany) {
		if (oneToMany) {
			return hasManyRelationshipData(metaResource);
		}
		return hasOneRelationshipData(metaResource);
	}

	private Schema relationship(MetaResource metaResource, boolean oneToMany, boolean nullable) {
		Schema schema = new ObjectSchema()
				.addProperties(
						"links",
						new Schema()
								.type("object")
								.addProperties(
										"self",
										new Schema()
												.type("string")
												.description("Relationship link for " + metaResource.getResourceType()))
								.addProperties(
										"related",
										new Schema()
												.type("object")
												.description("Related " + metaResource.getResourceType() + " link")
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
				getRelationshipSchema(metaResource, oneToMany));
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
		return Collections.singletonList(OpenAPIGeneratorConfig.class);
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
