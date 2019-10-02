package io.crnk.gen.openapi.internal;

import io.crnk.gen.openapi.OpenAPIGeneratorConfig;
import io.crnk.gen.openapi.internal.parameters.NestedFilter;
import io.crnk.gen.openapi.internal.parameters.PageLimit;
import io.crnk.gen.openapi.internal.parameters.PageNumber;
import io.crnk.gen.openapi.internal.parameters.PageOffset;
import io.crnk.gen.openapi.internal.parameters.PageSize;
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
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OASGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(OASGenerator.class);

  private File outputDir;

  private MetaLookup lookup;

  private OpenAPIGeneratorConfig config;

  private OpenAPI openApi;

  private Map<String, OASResource> oasResources = new HashMap<>();

  public OASGenerator(File outputDir, MetaLookup lookup, OpenAPIGeneratorConfig config) {
    this.outputDir = outputDir;
    this.lookup = lookup;
    this.config = config;

    openApi = config.getOpenAPI();
    LOGGER.debug("Adding static schemas");
    openApi.getComponents().addSchemas(new ApiError().getName(), new ApiError().schema());
    openApi.getComponents().addSchemas(new ResponseMixin().getName(), new ResponseMixin().schema());
    openApi.getComponents().addSchemas(new ListResponseMixin().getName(), new ListResponseMixin().schema());
    LOGGER.debug("Adding static parameters");
    openApi.getComponents().addParameters(new PageLimit().getName(), new PageLimit().parameter());
    openApi.getComponents().addParameters(new PageOffset().getName(), new PageOffset().parameter());
    boolean NumberSizePagingBehavior = false;
    if (NumberSizePagingBehavior) {  // TODO: Figure out how to determine this
      openApi.getComponents().addParameters(new PageSize().getName(), new PageNumber().parameter());
      openApi.getComponents().addParameters(new PageNumber().getName(), new PageSize().parameter());
    }
    openApi.getComponents().addParameters(new NestedFilter().getName(), new NestedFilter().parameter());
    LOGGER.debug("Adding static responses");
    openApi.getComponents().responses(generateStandardApiResponses());
    registerMetaResources();
  }

  public void run() throws IOException {
    buildPaths();
    writeSources();
  }

  private OpenAPI buildPaths() {

    for (OASResource oasResource : oasResources.values()) {

      LOGGER.debug("Adding resource list paths for {}", oasResource.getResourceName());
      PathItem resourcesPathItem = openApi.getPaths().getOrDefault(oasResource.getResourcesPath(), new PathItem());
      for (Map.Entry<OperationType, Operation> entry : oasResource.generateResourcesOperations().entrySet()) {
        openApi.getPaths().addPathItem(oasResource.getResourcesPath(), entry.getKey().merge(resourcesPathItem, entry.getValue()));
      }

      LOGGER.debug("Adding resource paths for {}", oasResource.getResourceName());
      PathItem resourcePathItem = openApi.getPaths().getOrDefault(oasResource.getResourcePath(), new PathItem());
      for (Map.Entry<OperationType, Operation> entry : oasResource.generateResourceOperations().entrySet()) {
        openApi.getPaths().addPathItem(oasResource.getResourcePath(), entry.getKey().merge(resourcePathItem, entry.getValue()));
      }

      // Relationships can be accessed in 2 ways:
      //  1.	/api/A/1/b                The full related resource
      //  2.	/api/A/1/relationships/b  The "ids" as belong to the resource
      // Generate GET Operations for /api/A/1/B relationship path
      for (MetaElement child : oasResource.getChildren()) {
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
        oasResource.getAttributes().put(mrf.getName(), attributeSchema);
        if (mrf.isAssociation()) {
          MetaResource relatedMetaResource = (MetaResource) mrf.getType().getElementType();
          OASResource relatedOasResource = oasResources.get(relatedMetaResource.getName());

          LOGGER.debug("Adding field path /{} of type {} for {}", mrf.getName(), relatedMetaResource.getResourceType(), oasResource.getResourceName());
          PathItem fieldPathItem = openApi.getPaths().getOrDefault(oasResource.getResourcePath() + oasResource.getResourcesPath(), new PathItem());
          for (Map.Entry<OperationType, Operation> entry : oasResource.generateFieldOperationsForField(relatedMetaResource, mrf).entrySet()) {
            openApi.getPaths().addPathItem(oasResource.getFieldPath(relatedOasResource), entry.getKey().merge(fieldPathItem, entry.getValue()));
          }

          LOGGER.debug("Adding field path relationships/{} of type {} for {}", mrf.getName(), relatedMetaResource.getResourceType(), oasResource.getResourceName());
          PathItem relationshipPathItem = openApi.getPaths().getOrDefault(oasResource.getResourcePath() + "/relationships" + relatedOasResource.getResourcesPath(), new PathItem());
          for (Map.Entry<OperationType, Operation> entry : oasResource.generateRelationshipsOperationsForField(relatedMetaResource, mrf).entrySet()) {
            openApi.getPaths().addPathItem(oasResource.getRelationshipsPath(relatedOasResource), entry.getKey().merge(relationshipPathItem, entry.getValue()));
          }
        }
      }
    }
    return openApi;
  }

  private void writeSources() throws IOException {
    File file = new File(outputDir, "openapi.yaml");
    write(file, Yaml.pretty(openApi));
  }

  private static void write(File file, String source) throws IOException {
    file.getParentFile().mkdirs();
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(source);
    }
  }

  private OpenAPI registerMetaResources() {
    List<MetaResource> metaResources = getJsonApiResources(lookup);
    metaResources.stream().map(OASResource::new).forEach(this::register);
    return openApi;
  }

  private OpenAPI register(OASResource oasResource) {
    LOGGER.debug("Adding generated parameters for {}", oasResource.getResourceName());
    oasResource.getComponentParameters().forEach(openApi.getComponents()::addParameters);
    LOGGER.debug("Adding generated schemas for {}", oasResource.getResourceName());
    oasResource.getComponentSchemas().forEach(openApi.getComponents()::addSchemas);
    LOGGER.debug("Adding generated responses for {}", oasResource.getResourceName());
    oasResource.getComponentResponses().forEach(openApi.getComponents()::addResponses);
    oasResources.put(oasResource.getResourceName(), oasResource);
    return openApi;
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

  // RESPONSES

  private Map<String, ApiResponse> generateStandardApiResponses() {
    return OASUtils.mergeApiResponses(generateStandardApiSuccessResponses(), OASErrors.generateStandardApiErrorResponses());
  }

  private Map<String, ApiResponse> generateStandardApiSuccessResponses() {
    Map<String, ApiResponse> responses = new LinkedHashMap<>();
    responses.put(new Accepted().getName(), new Accepted().response());
    responses.put(new NoContent().getName(), new NoContent().response());

    return responses;
  }
}
