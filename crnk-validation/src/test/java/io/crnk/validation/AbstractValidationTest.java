package io.crnk.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.client.CrnkClient;
import io.crnk.client.RelationshipRepositoryStub;
import io.crnk.client.ResourceRepositoryStub;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.rs.CrnkFeature;
import io.crnk.validation.mock.models.Project;
import io.crnk.validation.mock.models.Task;
import io.crnk.validation.mock.repository.TaskRepository;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.concurrent.TimeUnit;

public abstract class AbstractValidationTest extends JerseyTest {

	protected CrnkClient client;

	protected ResourceRepositoryStub<Task, Long> taskRepo;

	protected ResourceRepositoryStub<Project, Long> projectRepo;

	protected RelationshipRepositoryStub<Task, Long, Project, Long> relRepo;

	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());

	@Before
	public void setup() {
		client = new CrnkClient(getBaseUri().toString());
		client.addModule(ValidationModule.newInstance());
		taskRepo = client.getQueryParamsRepository(Task.class);
		projectRepo = client.getQueryParamsRepository(Project.class);
		relRepo = client.getQueryParamsRepository(Task.class, Project.class);
		TaskRepository.map.clear();

		client.getHttpAdapter().setReceiveTimeout(1000000, TimeUnit.MILLISECONDS);
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@ApplicationPath("/")
	private static class TestApplication extends ResourceConfig {

		public TestApplication() {
			property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, getClass().getPackage().getName());
			property(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");

			CrnkFeature feature = new CrnkFeature(new ObjectMapper(),
					new QueryParamsBuilder(new DefaultQueryParamsParser()), new SampleJsonServiceLocator());
			feature.addModule(ValidationModule.newInstance());
			register(feature);

		}
	}
}
