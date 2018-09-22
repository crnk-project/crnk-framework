package io.crnk.client;

import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class QuerySpecDisallowUnknownAttributeClientTest extends AbstractClientTest {

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	protected ResourceRepositoryV2<Project, Long> projectRepo;

	protected RelationshipRepositoryV2<Task, Long, Project, Long> relRepo;

	private DefaultQuerySpecUrlMapper urlMapper;

	@Before
	public void setup() {
		super.setup();

		DefaultQuerySpecUrlMapper urlMapper = (DefaultQuerySpecUrlMapper) client.getUrlMapper();
		urlMapper.setAllowUnknownAttributes(false);

		taskRepo = client.getRepositoryForType(Task.class);
		projectRepo = client.getRepositoryForType(Project.class);
		relRepo = client.getRepositoryForType(Task.class, Project.class);
	}

	@Override
	protected TestApplication configure() {
		TestApplication app = new TestApplication();
		urlMapper = (DefaultQuerySpecUrlMapper) app.getFeature().getUrlMapper();
		urlMapper.setAllowUnknownAttributes(true);
		return app;
	}

	@Test
	public void testCall() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("unknownAttr"), Direction.ASC));
		querySpec.addFilter(new FilterSpec(Arrays.asList("unknownAttr"), FilterOperator.EQ, "test"));
		try {
			taskRepo.findAll(querySpec);
		} catch (BadRequestException e) {
			Assert.assertEquals("Failed to resolve path to field 'unknownAttr'", e.getMessage());
		}
	}

}