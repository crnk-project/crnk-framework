package io.crnk.rs;

import static io.crnk.rs.type.JsonApiMediaType.APPLICATION_JSON_API_TYPE;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.crnk.rs.controller.SampleControllerWithPrefix;
import io.crnk.test.JerseyTestBase;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.TaskRepository;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UriInfoServiceUrlProviderTest extends JerseyTestBase {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new JettyTestContainerFactory();
    }

    @Override
    protected Application configure() {
        return new TestApplication();
    }


    @Before
    public void setup() {
        TaskRepository repo = new TaskRepository();

        Task task = new Task();
        task.setName("test");
        task.setId(1L);
        repo.save(task);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        TaskRepository.clear();
    }


    @Test
    public void testLinkToHaveValidUrl() {
        String headerTestValue = "test value";
        String taskResourceResponse = target("/tasks/1").request(APPLICATION_JSON_API_TYPE).header("X-test", headerTestValue)
                .get(String.class);

        assertThatJson(taskResourceResponse).node("data.relationships.project.links.self")
                .isStringEqualTo(this.target().getUri().toString() + "tasks/1/relationships/project");
        assertThatJson(taskResourceResponse).node("data.relationships.project.links.related")
                .isStringEqualTo(this.target().getUri().toString() + "tasks/1/project");

    }

    @ApplicationPath("/")
    private static class TestApplication extends ResourceConfig {

        public TestApplication() {
            register(SampleControllerWithPrefix.class);

            CrnkFeature feature = new CrnkFeature();
            feature.addModule(new TestModule());
            register(feature);
        }
    }
}
