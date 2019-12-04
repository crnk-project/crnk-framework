package io.crnk.client;

import java.util.ArrayList;
import java.util.List;

import io.crnk.test.mock.models.BulkTask;
import io.crnk.test.mock.repository.BulkTaskRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BulkClientTest extends AbstractClientTest {

	private static final String EXPECTED_CONTENT_TYPE = "application/vnd.api+json";

	protected BulkTaskRepository taskRepo;

	@Before
	public void setup() {
		super.setup();

		taskRepo = client.getRepositoryForInterface(BulkTaskRepository.class);
	}

	@Test
	public void test() {
		List<BulkTask> tasks = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			BulkTask task = new BulkTask();
			task.setId((long) i);
			task.setName("task" + i);
			tasks.add(task);
		}

		List<BulkTask> createdTasks = taskRepo.create(tasks);
		Assert.assertEquals(10, createdTasks.size());
		Assert.assertEquals("task0", createdTasks.get(0).getName());
	}
}
