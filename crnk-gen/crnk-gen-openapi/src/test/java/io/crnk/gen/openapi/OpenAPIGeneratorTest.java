package io.crnk.gen.openapi;

import io.crnk.client.CrnkClient;
import io.crnk.client.http.inmemory.InMemoryHttpAdapter;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.format.plainjson.PlainJsonFormatModule;
import io.crnk.gen.openapi.OpenAPIGeneratorModule;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.TaskStatus;
import io.crnk.test.suite.TestContainer;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class OpenAPIGeneratorTest {

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
	public void checkAccess() {
		Task newTask = new Task();
		newTask.setName("Favorite Task");
		newTask.setStatus(TaskStatus.OPEN);

		Task createdTask = taskRepository.create(newTask);

		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(PathSpec.of("name").filter(FilterOperator.EQ, "Favorite Task"));
		querySpec.setOffset(0);
		querySpec.setLimit(5L);
		ResourceList<Task> list = taskRepository.findAll(querySpec);
		Assert.assertNotEquals(0, list.size());

		createdTask.setName("Updated Task");
		taskRepository.save(createdTask);

		taskRepository.delete(createdTask.getId());
	}

    @Test
    public void testGeneration() throws IOException {
		File buildDir = new File("build/tmp/openapi");
		generatorModule = new OpenAPIGeneratorModule();
        generatorModule.getConfig().setBuildDir(buildDir);
        generatorModule.initDefaults(buildDir);
        generatorModule.generate(metaModule.getLookup());
    }
}
