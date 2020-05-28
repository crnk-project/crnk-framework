package io.crnk.client;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.BulkTask;
import io.crnk.test.mock.repository.BulkTaskRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BulkClientTest extends AbstractClientTest {

	private static final String EXPECTED_CONTENT_TYPE = "application/vnd.api+json";

	protected BulkTaskRepository taskRepo;

	@Before
	public void setup() {
		super.setup();

		taskRepo = client.getRepositoryForInterface(BulkTaskRepository.class);
	}

	@Test
	public void testCreate() {
		testCreate(10);
	}

	@Test
	public void testCreateWithSingleElement() {
		testCreate(1);
	}

	private void testCreate(int n) {
		List<BulkTask> tasks = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			BulkTask task = new BulkTask();
			task.setId((long) i);
			task.setName("task" + i);
			tasks.add(task);
		}

		List<BulkTask> createdTasks = taskRepo.create(tasks);
		Assert.assertEquals(n, createdTasks.size());
		Assert.assertEquals("task0", createdTasks.get(0).getName());
	}

	@Test
	public void testDelete() {
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

		List<Long> taskIds = tasks.stream().map(BulkTask::getId).collect(Collectors.toList());
		taskRepo.delete(taskIds);
		QuerySpec querySpec = new QuerySpec(BulkTask.class);
		ResourceList<BulkTask> afterModTasks = taskRepo.findAll(querySpec);
		Assert.assertEquals(0, afterModTasks.size());
	}
}
