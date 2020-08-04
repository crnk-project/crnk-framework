package io.crnk.client;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.queryspec.*;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.PrimitiveAttributeResource;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.TaskRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.*;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class FilterCriteriaInBodyClientTest extends AbstractClientTest {
	private static final int PROJECT_COUNT = 10;

	private final ProjectRepository projectRepository = new ProjectRepository();
	private final TaskRepository taskRepository = new TaskRepository();

	@Before
	public void setup() {
		super.setup();
		createProjects();
	}

	private void createProjects() {
		for (int i = 0; i < PROJECT_COUNT; i++) {
			Task task = new Task();
			task.setId((long) i);
			task.setName("Task " + i);
			taskRepository.create(task);
			Project project = new Project();
			project.setId((long) i);
			project.setName("Project " + (char) ('A' + i));
			project.getTasks().add(task);
			if (i == 5) {
				project.setDescription("Test");
			}
			projectRepository.create(project);
		}
	}

	@Override
	protected TestApplication configure() {
		TestApplication app = super.configure();
		Map<String, String> properties = new HashMap<>();
		properties.put(CrnkProperties.FILTER_CRITERIA_IN_HTTP_BODY, Boolean.TRUE.toString());
		properties.put(CrnkProperties.ALLOW_UNKNOWN_ATTRIBUTES, Boolean.TRUE.toString());
		app.setProperties(properties);
		return app;
	}

	@Override
	protected void setupClient(CrnkClient client) {
		client.setHttpAdapter(new HttpClientAdapter());
		client.setFilterCriteriaInRequestBody(true);
		((DefaultQuerySpecUrlMapper) client.getUrlMapper()).setAllowUnknownAttributes(true);
		client.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		client.getObjectMapper().registerModule(new JavaTimeModule());
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
		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.addFilter(PathSpec.of("id").filter(FilterOperator.LT, 5));
		ResourceRepository<Project, Long> projectRepo = client.getRepositoryForType(Project.class);
		ResourceList<Project> projects = projectRepo.findAll(querySpec);
		assertNotNull(projects);
		assertEquals(4, projects.size());
	}

	@Test
	public void testWithCombinedFilter() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.addFilter(FilterSpec.or(
				PathSpec.of("id").filter(FilterOperator.LT, 5),
				PathSpec.of("description").filter(FilterOperator.EQ, "Test")));
		ResourceRepository<Project, Long> projectRepo = client.getRepositoryForType(Project.class);
		ResourceList<Project> projects = projectRepo.findAll(querySpec);
		assertNotNull(projects);
		assertEquals(5, projects.size());
	}

	@Test(expected = InternalServerErrorException.class)
	public void testUnknownFilterAttributeIsPassedToServer() {
		ResourceRepository<Project, Long> projectRepo = client.getRepositoryForType(Project.class);
		QuerySpec querySpec = new QuerySpec(Project.class);
		String unknownPath = "unknown";
		querySpec.addFilter(PathSpec.of(unknownPath).filter(FilterOperator.EQ, 1));
		projectRepo.findAll(querySpec);
	}

	@Test
	public void filterByDateWithPlainDate() {
		Date now = new Date();
		createResource(now);

		QuerySpec querySpec = new QuerySpec(PrimitiveAttributeResource.class);
		Date max = DateUtils.addDays(now, 1);
		querySpec.addFilter(PathSpec.of("dateValue").filter(FilterOperator.LT, max));
		ResourceRepository<PrimitiveAttributeResource, Long> primitiveRepo = client.getRepositoryForType(PrimitiveAttributeResource.class);
		ResourceList<PrimitiveAttributeResource> resources = primitiveRepo.findAll(querySpec);
		assertNotNull(resources);
		assertEquals(1, resources.size());
	}

	private void createResource(Date date) {
		ResourceRepository<PrimitiveAttributeResource, Long> primitiveRepo = client.getRepositoryForType(PrimitiveAttributeResource.class);
		PrimitiveAttributeResource resource = new PrimitiveAttributeResource();
		resource.setId(1L);
		resource.setDateValue(date);
		primitiveRepo.create(resource);
	}

	@Test
	public void filterByDateWithLocalDate() {
		Date now = new Date();
		createResource(now);

		QuerySpec querySpec = new QuerySpec(PrimitiveAttributeResource.class);
		LocalDate max = LocalDate.now().plusDays(-1);
		querySpec.addFilter(PathSpec.of("dateValue").filter(FilterOperator.GT, max));
		ResourceRepository<PrimitiveAttributeResource, Long> primitiveRepo = client.getRepositoryForType(PrimitiveAttributeResource.class);
		ResourceList<PrimitiveAttributeResource> resources = primitiveRepo.findAll(querySpec);
		assertNotNull(resources);
		assertEquals(1, resources.size());
	}

	@Test
	public void testListOfFiltersIsSerializedCorrectly() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.addFilter(PathSpec.of("tasks.id").filter(FilterOperator.EQ, Arrays.asList(3L, 4L)));
		querySpec.addFilter(PathSpec.of("tasks.name").filter(FilterOperator.EQ, Arrays.asList("Task 4", "Task 5")));
		ResourceRepository<Project, Long> projectRepo = client.getRepositoryForType(Project.class);
		ResourceList<Project> projects = projectRepo.findAll(querySpec);
		assertNotNull(projects);
		assertEquals(1, projects.size());
	}
}
