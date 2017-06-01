package io.crnk.client;

import java.util.List;

import io.crnk.client.http.AbstractClientTest;
import io.crnk.client.legacy.ResourceRepositoryStub;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HeadersTest extends AbstractClientTest {
	private static final String EXPECTED_CONTENT_TYPE = "application/vnd.api+json";

	protected ResourceRepositoryStub<Task, Long> taskRepo;

	@Before
	public void setup() {
		super.setup();

		taskRepo = client.getQueryParamsRepository(Task.class);
	}

	@Test
	public void testClientHeaders() {
		clearLastReceivedHeaders();

		List<Task> tasks = taskRepo.findAll(new QueryParams());
		Assert.assertTrue(tasks.isEmpty());

		assertHasHeaderValue("Accept", EXPECTED_CONTENT_TYPE);
		assertHasHeaderValue("Content-Type", EXPECTED_CONTENT_TYPE);
	}
}
