package io.crnk.gen.openapi;

import io.crnk.client.CrnkClient;
import io.crnk.client.http.inmemory.InMemoryHttpAdapter;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class OpenAPIGeneratorComplexTest extends OpenAPIGeneratorTestBase {
  private MetaModule metaModule;
  private OpenAPIGeneratorModule generatorModule;

  private ResourceRepository<Task, Long> taskRepository;
  private ResourceRepository<Project, Long> projectRepository;

  private OneRelationshipRepository<Task, Long, Project, Long> taskToProjectOneRelationRepository;
  private ManyRelationshipRepository<Task, Long, Project, Long> taskToProjectManyRelationRepository;

  @Before
  public void setup() {
    CrnkBoot boot = setupServer();
    CrnkClient client = setupClient(boot);

    taskRepository = client.getRepositoryForType(Task.class);
    projectRepository = client.getRepositoryForType(Project.class);
    taskToProjectOneRelationRepository = client.getOneRepositoryForType(Task.class, Project.class);
    taskToProjectManyRelationRepository = client.getManyRepositoryForType(Task.class, Project.class);
  }

  private CrnkClient setupClient(CrnkBoot boot) {
    String baseUrl = "http://127.0.0.1:8080/api";
    InMemoryHttpAdapter httpAdapter = new InMemoryHttpAdapter(boot, baseUrl);

    CrnkClient client = new CrnkClient(baseUrl);
    client.setHttpAdapter(httpAdapter);
    return client;
  }

  private CrnkBoot setupServer() {
    MetaModuleConfig metaConfig = new MetaModuleConfig();
    metaConfig.addMetaProvider(new ResourceMetaProvider());
    metaModule = MetaModule.createServerModule(metaConfig);

    CrnkBoot boot = new CrnkBoot();
    boot.setServiceDiscovery(new EmptyServiceDiscovery());
    boot.addModule(new TestModule());
    boot.addModule(metaModule);
    boot.boot();
    return boot;
  }

  @Test
  public void testGeneration() throws IOException {
    File buildDir = new File("build/tmp/openapi");
    String outputPath = buildDir.toString() + "/generated/source/openapi/openapi.yaml";
    generatorModule = new OpenAPIGeneratorModule();
    generatorModule.getConfig().setBuildDir(buildDir);
    generatorModule.initDefaults(buildDir);
    generatorModule.generate(metaModule.getLookup());
    // Parsable
    new OpenAPIV3Parser().read(outputPath);

    OpenAPI openApi = new OpenAPIV3Parser().read(outputPath);

    Assert.assertEquals("Generated Title", openApi.getInfo().getTitle());
    Assert.assertEquals("Generated Description", openApi.getInfo().getDescription());
    Assert.assertEquals("0.1.0", openApi.getInfo().getVersion());

    // Ensure responses satisfy minimal requirements for JSON:API compliance
    for (Map.Entry<String, PathItem> entry : openApi.getPaths().entrySet()) {
      assertJsonAPICompliantPath(entry.getKey(), entry.getValue());
      assertResponsesSorted(entry.getValue());
    }
  }
}
