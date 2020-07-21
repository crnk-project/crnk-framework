package io.crnk.client;

import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Project;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class FilterCriteriaInBodyClientTest extends AbstractClientTest {
	private static final int PROJECT_COUNT = 10;

	private ResourceRepository<Project, Long> projectRepo;

	@Before
	public void setup() {
		super.setup();
		projectRepo = client.getRepositoryForType(Project.class);
		createProjects();
	}

	private void createProjects() {
		for (long i = 0; i < PROJECT_COUNT; i++) {
			Project project = new Project();
			project.setName("Project " + (char) ('A' + i));
			if (i == 5) {
				project.setDescription("Test");
			}
			projectRepo.create(project);
		}
	}

	@Override
	protected TestApplication configure() {
		TestApplication app = super.configure();
		app.setProperties(Collections.singletonMap(CrnkProperties.FILTER_CRITERIA_IN_HTTP_BODY, Boolean.TRUE.toString()));
		return app;
	}

	@Override
	protected void setupClient(CrnkClient client) {
		client.setHttpAdapter(new HttpClientAdapter());
		client.setFilterCriteriaInRequestBody(true);
	}

	@Test
	public void testWithoutFilter() {
		ResourceRepository<Project, Long> projectRepo = client.getRepositoryForType(Project.class);
		ResourceList<Project> projects = projectRepo.findAll(new QuerySpec(Project.class));
		assertNotNull(projects);
		assertEquals(PROJECT_COUNT, projects.size());
	}

	@Test
	public void testWithSingleFilter() {
		ResourceRepository<Project, Long> projectRepo = client.getRepositoryForType(Project.class);
		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.addFilter(PathSpec.of("id").filter(FilterOperator.LT, 5));
		ResourceList<Project> projects = projectRepo.findAll(querySpec);
		assertNotNull(projects);
		assertEquals(4, projects.size());
	}

	@Test
	public void testWithCombinedFilter() {
		ResourceRepository<Project, Long> projectRepo = client.getRepositoryForType(Project.class);
		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.addFilter(FilterSpec.or(
				PathSpec.of("id").filter(FilterOperator.LT, 5),
				PathSpec.of("description").filter(FilterOperator.EQ, "Test")));
		ResourceList<Project> projects = projectRepo.findAll(querySpec);
		assertNotNull(projects);
		assertEquals(5, projects.size());
	}
}
