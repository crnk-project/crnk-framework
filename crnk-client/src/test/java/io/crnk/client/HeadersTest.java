package io.crnk.client;

import io.crnk.client.legacy.ResourceRepositoryStub;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class HeadersTest extends AbstractClientTest {

	private static final String EXPECTED_CONTENT_TYPE = "application/vnd.api+json";

	protected ResourceRepositoryStub<Task, Long> taskRepo;

	@Before
	public void setup() {
		super.setup();

		taskRepo = client.getQueryParamsRepository(Task.class);
	}

	@Test
	public void testClientHeadersOnGet() {
		clearLastReceivedHeaders();

		List<Task> tasks = taskRepo.findAll(new QueryParams());
		Assert.assertTrue(tasks.isEmpty());

		assertHasHeaderValue("Accept", EXPECTED_CONTENT_TYPE);
	}

	@Test
	public void testClientHeadersOnPst() {
		clearLastReceivedHeaders();

		Task task = new Task();
		task.setId(1L);
		task.setName("test");
		taskRepo.create(task);

		assertHasHeaderValue("Accept", EXPECTED_CONTENT_TYPE);
		assertHasHeaderValue("Content-Type", HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET);
	}
}
