package io.crnk.validation;

import io.crnk.client.CrnkClient;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.JerseyTestBase;
import io.crnk.validation.mock.models.Project;
import io.crnk.validation.mock.models.Task;
import io.crnk.validation.mock.repository.TaskRepository;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.concurrent.TimeUnit;

public abstract class AbstractValidationTest extends JerseyTestBase {

	protected CrnkClient client;

	protected ResourceRepository<Task, Long> taskRepo;

	protected ResourceRepository<Project, Long> projectRepo;

	protected RelationshipRepositoryV2<Task, Long, Project, Long> relRepo;

	@Before
	public void setup() {
		client = new CrnkClient(getBaseUri().toString());
		client.addModule(ValidationModule.newInstance());
		taskRepo = client.getRepositoryForType(Task.class);
		projectRepo = client.getRepositoryForType(Project.class);
		relRepo = client.getRepositoryForType(Task.class, Project.class);
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

			CrnkFeature feature = new CrnkFeature();
			feature.addModule(ValidationModule.create());
			register(feature);

		}
	}
}
