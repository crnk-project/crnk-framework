package io.crnk.gen.asciidoc;

import io.crnk.client.CrnkClient;
import io.crnk.client.http.inmemory.InMemoryHttpAdapter;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.format.plainjson.PlainJsonFormatModule;
import io.crnk.gen.asciidoc.capture.AsciidocCaptureConfig;
import io.crnk.gen.asciidoc.capture.AsciidocCaptureModule;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.TaskStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class AsciiDocCaptureTest {

    private ResourceRepository<Task, Long> taskRepository;
    private ResourceRepository<Project, Long> projectRepository;

    private OneRelationshipRepository<Task, Long, Project, Long> taskToProjectOneRelationRepository;
    private ManyRelationshipRepository<Task, Long, Project, Long> taskToProjectManyRelationRepository;

    private AsciidocCaptureModule asciidoc;

    @Before
    public void setup() {
        CrnkBoot boot = setupServer();

        asciidoc = setupAsciidoc();
        CrnkClient client = setupClient(boot, asciidoc);
        taskRepository = client.getRepositoryForType(Task.class);
        projectRepository = client.getRepositoryForType(Project.class);
        taskToProjectOneRelationRepository = client.getOneRepositoryForType(Task.class, Project.class);
        taskToProjectManyRelationRepository = client.getManyRepositoryForType(Task.class, Project.class);
    }

    private CrnkClient setupClient(CrnkBoot boot, AsciidocCaptureModule module) {
        String baseUrl = "http://127.0.0.1:8080/api";
        InMemoryHttpAdapter httpAdapter = new InMemoryHttpAdapter(boot, baseUrl);

        CrnkClient client = new CrnkClient(baseUrl);
        client.addModule(module);
        client.addModule(new PlainJsonFormatModule());
        client.setHttpAdapter(httpAdapter);
        return client;
    }

    private AsciidocCaptureModule setupAsciidoc() {
        File outputDir = new File("build/tmp/asciidoc/generated/source/asciidoc");
        AsciidocCaptureConfig asciidocConfig = new AsciidocCaptureConfig();
        asciidocConfig.setGenDir(outputDir);
        return new AsciidocCaptureModule(asciidocConfig);
    }

    private CrnkBoot setupServer() {
        CrnkBoot boot = new CrnkBoot();
        boot.addModule(new TestModule());
        boot.addModule(new PlainJsonFormatModule());
        boot.boot();
        return boot;
    }

    @Test
    public void checkAccess() {
        Task newTask = new Task();
        newTask.setName("Favorite Task");
        newTask.setStatus(TaskStatus.OPEN);

        Task createdTask = asciidoc.capture("Create new Task").call(() -> taskRepository.create(newTask));

        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(PathSpec.of("name").filter(FilterOperator.EQ, "Favorite Task"));
        querySpec.setOffset(0);
        querySpec.setLimit(5L);
        ResourceList<Task> list = asciidoc.capture("Find Task by Name").call(() -> taskRepository.findAll(querySpec));
        Assert.assertNotEquals(0, list.size());

        createdTask.setName("Updated Task");
        asciidoc.capture("Update a Task").call(() -> taskRepository.save(createdTask));

        asciidoc.capture("Delete a Task").call(() -> taskRepository.delete(createdTask.getId()));
    }

	@Test
	public void checkOneRelationRepositoryAccess() {
		Task newTask = new Task();
		newTask.setName("Favorite Task");
		newTask.setStatus(TaskStatus.OPEN);
		Task createdTask = taskRepository.create(newTask);

		Project newProject = new Project();
		newProject.setName("Favorite Project");
		newProject.setDescription("It's just for test");
		Project createdProject = projectRepository.create(newProject);

		asciidoc.capture("Link Project and Task").call(() -> taskToProjectOneRelationRepository.setRelation(createdTask, createdProject.getId(), "project"));

		asciidoc.capture("Find Project by Task").call(() -> taskToProjectOneRelationRepository.findOneRelations(Collections.singleton(createdTask.getId()), "project", new QuerySpec(Project.class)));

		asciidoc.capture("Delete Project from Task").call(() -> taskToProjectOneRelationRepository.setRelation(createdTask, null, "project"));
	}

	@Test
	public void checkManyRelationRepositoryAccess() {
		Task newTask = new Task();
		newTask.setName("Favorite Task");
		newTask.setStatus(TaskStatus.OPEN);
		Task createdTask = taskRepository.create(newTask);

		Project firstNewProject = new Project();
		firstNewProject.setName("First Favorite Project");
		firstNewProject.setDescription("It's just for test");
		Project firstCreatedProject = projectRepository.create(firstNewProject);

		Project secondNewProject = new Project();
		secondNewProject.setName("Second Favorite Project");
		secondNewProject.setDescription("It's just for test");
		Project secondCreatedProject = projectRepository.create(secondNewProject);

		asciidoc.capture("Link Task with several Projects").call(() -> taskToProjectManyRelationRepository.addRelations(createdTask, Arrays.asList(firstCreatedProject.getId(), secondCreatedProject.getId()), "projects"));

		asciidoc.capture("Get Task relation Projects").call(() -> taskToProjectManyRelationRepository.findManyRelations(Collections.singleton(createdTask.getId()), "projects", new QuerySpec(Project.class)));

		asciidoc.capture("Remove single Project from Task").call(() -> taskToProjectManyRelationRepository.removeRelations(createdTask, Collections.singleton(firstCreatedProject.getId()), "projects"));
	}
}
