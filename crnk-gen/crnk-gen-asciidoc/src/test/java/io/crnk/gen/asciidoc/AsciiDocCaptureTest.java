package io.crnk.gen.asciidoc;

import io.crnk.client.CrnkClient;
import io.crnk.client.http.inmemory.InMemoryHttpAdapter;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.format.plainjson.PlainJsonFormatModule;
import io.crnk.gen.asciidoc.capture.AsciidocCaptureConfig;
import io.crnk.gen.asciidoc.capture.AsciidocCaptureModule;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.TaskStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class AsciiDocCaptureTest {

    private ResourceRepositoryV2<Task, Long> repository;

    private AsciidocCaptureModule asciidoc;

    @Before
    public void setup() {
        CrnkBoot boot = setupServer();

        asciidoc = setupAsciidoc();
        CrnkClient client = setupClient(boot, asciidoc);
        repository = client.getRepositoryForType(Task.class);
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

        Task createdTask = asciidoc.capture("Create new Task").call(() -> repository.create(newTask));

        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(PathSpec.of("name").filter(FilterOperator.EQ, "Favorite Task"));
        querySpec.setOffset(0);
        querySpec.setLimit(5L);
        ResourceList<Task> list = asciidoc.capture("Find Task by Name").call(() -> repository.findAll(querySpec));
        Assert.assertNotEquals(0, list.size());

        createdTask.setName("Updated Task");
        asciidoc.capture("Update a Task").call(() -> repository.save(createdTask));

        asciidoc.capture("Delete a Task").call(() -> repository.delete(createdTask.getId()));
    }
}
