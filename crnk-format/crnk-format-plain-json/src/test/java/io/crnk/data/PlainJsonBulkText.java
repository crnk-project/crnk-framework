package io.crnk.data;

import java.util.ArrayList;
import java.util.List;

import io.crnk.test.mock.models.BulkTask;
import io.crnk.test.mock.repository.BulkTaskRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PlainJsonBulkText {

	protected BulkTaskRepository taskRepo;

	private PlainJsonTestContainer container;

	@Before
	public void setup() {
		container = new PlainJsonTestContainer();
		container.start();
		taskRepo = container.getClient().getRepositoryForInterface(BulkTaskRepository.class);
	}

	@After
	public void tearDown() {
		container.stop();
	}

	@Test
	public void test() {
		List<BulkTask> tasks = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			BulkTask task = new BulkTask();
			task.setId((long) i);
			task.setName("bulkTask" + i);
			tasks.add(task);
		}

		List<BulkTask> createdTasks = taskRepo.create(tasks);
		Assert.assertEquals(10, createdTasks.size());
		Assert.assertEquals("bulkTask0", createdTasks.get(0).getName());
	}
}
