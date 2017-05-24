package io.crnk.client;

import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExceptionTest extends AbstractClientTest {

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	@Before
	public void setup() {
		super.setup();
		taskRepo = client.getRepositoryForType(Task.class);
	}

	@Test
	public void genericRepo() {
		Task task = new Task();
		task.setId(10000L);
		task.setName("test");
		try {
			taskRepo.create(task);
			Assert.fail();
		} catch (TestException e) {
			Assert.assertEquals("msg", e.getMessage());
		}
	}

	@Test
	public void repoWithProxyAndInterface() {
		ScheduleRepository repo = client.getRepositoryForInterface(ScheduleRepository.class);

		Schedule schedule = new Schedule();
		schedule.setId(10000L);
		schedule.setName("test");
		try {
			repo.create(schedule);
			Assert.fail();
		} catch (TestException e) {
			Assert.assertEquals("msg", e.getMessage());
		}
	}
}