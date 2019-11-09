package io.crnk.gen.openapi.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.annotations.VisibleForTesting;
import io.crnk.gen.openapi.OpenAPIGeneratorConfig;
import io.crnk.gen.openapi.OutputFormat;
import io.crnk.gen.openapi.internal.operations.OASOperation;
import io.crnk.gen.openapi.internal.parameters.NestedFilter;
import io.crnk.gen.openapi.internal.parameters.PageLimit;
import io.crnk.gen.openapi.internal.parameters.PageNumber;
import io.crnk.gen.openapi.internal.parameters.PageOffset;
import io.crnk.gen.openapi.internal.parameters.PageSize;
import io.crnk.gen.openapi.internal.responses.StaticResponses;
import io.crnk.gen.openapi.internal.schemas.ApiError;
import io.crnk.gen.openapi.internal.schemas.Failure;
import io.crnk.gen.openapi.internal.schemas.Info;
import io.crnk.gen.openapi.internal.schemas.JsonApi;
import io.crnk.gen.openapi.internal.schemas.Link;
import io.crnk.gen.openapi.internal.schemas.Links;
import io.crnk.gen.openapi.internal.schemas.Meta;
import io.crnk.gen.openapi.internal.schemas.Pagination;
import io.crnk.gen.openapi.internal.schemas.Success;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
    openApi.getComponents().addSchemas(new Failure().getName(), new Failure().schema());
    openApi.getComponents().addSchemas(new Info().getName(), new Info().schema());
    openApi.getComponents().addSchemas(new JsonApi().getName(), new JsonApi().schema());
    openApi.getComponents().addSchemas(new Link().getName(), new Link().schema());
    openApi.getComponents().addSchemas(new Links().getName(), new Links().schema());
    openApi.getComponents().addSchemas(new Meta().getName(), new Meta().schema());
    openApi.getComponents().addSchemas(new Pagination().getName(), new Pagination().schema());
    openApi.getComponents().addSchemas(new Success().getName(), new Success().schema());
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
    openApi.getComponents().responses(StaticResponses.generateStandardApiResponses());
    registerMetaResources();
  }

  public void run() throws IOException {
    buildPaths();
    writeSources();
  }

  private OpenAPI buildPaths() {
    for (OASResource oasResource : oasResources.values()) {
      for (OASOperation oasOperation : oasResource.getOperations()) {
        PathItem resourcesPathItem = openApi.getPaths().getOrDefault(oasOperation.path(), new PathItem());
        openApi.getPaths().addPathItem(oasOperation.path(), oasOperation.operationType().merge(resourcesPathItem, oasOperation.operation()));
      }
    }
    return openApi;
  }

  @VisibleForTesting
  static String generateOpenApiContent(OpenAPI openApi, OutputFormat outputFormat, Boolean sort) {
    if (sort) {
      ObjectMapper objectMapper = outputFormat.mapper();
      objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
      objectMapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
      try {
        return objectMapper.writer(new DefaultPrettyPrinter()).writeValueAsString(openApi);
      } catch (JsonProcessingException e) {
        LOGGER.error("Sorting failed!");
        return outputFormat.pretty(openApi);
      }
    } else {
      return outputFormat.pretty(openApi);
    }
  }

  private void writeSources() throws IOException {
    OutputFormat outputFormat = config.getOutputFormat();
    File file = new File(outputDir, "openapi." + outputFormat.extension());
    write(file, generateOpenApiContent(openApi, outputFormat, config.getOutputSorted()));
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
}
