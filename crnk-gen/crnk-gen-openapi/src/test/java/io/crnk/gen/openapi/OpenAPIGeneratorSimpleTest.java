package io.crnk.gen.openapi;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.gen.openapi.mock.SimpleTestModule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class OpenAPIGeneratorSimpleTest extends OpenAPIGeneratorTestBase {

  private CrnkBoot crnkBoot;
  private MetaModule metaModule;
  private OpenAPIGeneratorModule generatorModule;

  @Before
  public void setup() throws IOException {
    File buildDir = new File("build/tmp/openapi");

    InputStream xmlDoc = getClass().getClassLoader().getResourceAsStream("openapi-template.yml");
    FileUtils.copyInputStreamToFile(xmlDoc, new File(buildDir, "openapi-template.yml"));

    MetaModuleConfig metaConfig = new MetaModuleConfig();
    metaConfig.addMetaProvider(new ResourceMetaProvider());
    metaModule = MetaModule.createServerModule(metaConfig);

    crnkBoot = new CrnkBoot();
    crnkBoot.setServiceDiscovery(new EmptyServiceDiscovery());
    crnkBoot.addModule(new SimpleTestModule());
    crnkBoot.addModule(metaModule);
    crnkBoot.boot();
  }

  @Test
  public void testGeneration() throws IOException {
    File buildDir = new File("build/tmp/openapi");
    String outputPath = buildDir.toString() + "/generated/source/openapi/openapi.yaml";
    String templatePath = buildDir.toString() + "/openapi-template.yml";
    generatorModule = new OpenAPIGeneratorModule();
    generatorModule.getConfig().setBuildDir(buildDir);
    generatorModule.getConfig().setTemplateName("openapi-template.yml");
    generatorModule.getConfig().setOutputSorted(true);  // Ensures deterministic output
    generatorModule.initDefaults(buildDir);
    generatorModule.generate(metaModule.getLookup());
    OpenAPI openApi = new OpenAPIV3Parser().read(outputPath);
    OpenAPI openApiTemplate = new OpenAPIV3Parser().read(templatePath);

    // Compare templated metadata and generated metadata
    Assert.assertEquals(openApiTemplate.getOpenapi(), openApi.getOpenapi());
    Assert.assertEquals(openApiTemplate.getInfo(), openApi.getInfo());
    Assert.assertEquals(openApiTemplate.getServers(), openApi.getServers());

    Operation templateGetTasks = openApiTemplate.getPaths().get("/simpleTasks").getGet();
    Operation generatedGetTasks = openApi.getPaths().get("/simpleTasks").getGet();

    // Compare templated and generated GET /simpleTasks
    Assert.assertEquals(templateGetTasks.getSummary(), generatedGetTasks.getSummary());
    Assert.assertEquals(templateGetTasks.getDescription(), generatedGetTasks.getDescription());
    Assert.assertEquals(templateGetTasks.getOperationId(), generatedGetTasks.getOperationId());
    Assert.assertEquals(templateGetTasks.getExtensions(), generatedGetTasks.getExtensions());

    // Compare templated and generated GET /simpleTasks
    Operation templateGetTaskById = openApiTemplate.getPaths().get("/simpleTasks/{id}").getGet();
    Operation generatedGetTaskById = openApi.getPaths().get("/simpleTasks/{id}").getGet();
    Assert.assertEquals(templateGetTaskById.getExtensions(), generatedGetTaskById.getExtensions());

    // Ensure responses satisfy minimal requirements for JSON:API compliance
    for (Map.Entry<String, PathItem> entry : openApi.getPaths().entrySet()) {
      assertJsonAPICompliantPath(entry.getKey(), entry.getValue());
      assertResponsesSorted(entry.getValue());
    }
    compare("gold/simple.yaml", outputPath);
  }
}
