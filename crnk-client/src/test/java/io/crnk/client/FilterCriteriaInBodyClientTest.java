package io.crnk.client;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.PrimitiveAttributeResource;
import io.crnk.test.mock.models.Project;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class FilterCriteriaInBodyClientTest extends AbstractClientTest {
	private static final int PROJECT_COUNT = 10;

	private ResourceRepository<Project, Long> projectRepo;
	private ResourceRepository<PrimitiveAttributeResource, Object> primitiveRepo;

	@Before
	public void setup() {
		super.setup();
		projectRepo = client.getRepositoryForType(Project.class);
		createProjects();
		primitiveRepo = client.getRepositoryForType(PrimitiveAttributeResource.class);
	}

	private void createProjects() {
		OffsetDateTime dueBase = OffsetDateTime.now();
		for (int i = 0; i < PROJECT_COUNT; i++) {
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
		ResourceList<Project> projects = projectRepo.findAll(new QuerySpec(Project.class));
		assertNotNull(projects);
		assertEquals(PROJECT_COUNT, projects.size());
	}

	@Test
	public void testWithSingleFilter() {
		QuerySpec querySpec = new QuerySpec(Project.class);
		querySpec.addFilter(PathSpec.of("id").filter(FilterOperator.LT, 5));
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
		ResourceList<PrimitiveAttributeResource> resources = primitiveRepo.findAll(querySpec);
		assertNotNull(resources);
		assertEquals(1, resources.size());
	}

	private void createResource(Date date) {
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
		ResourceList<PrimitiveAttributeResource> resources = primitiveRepo.findAll(querySpec);
		assertNotNull(resources);
		assertEquals(1, resources.size());
	}
}
