package io.crnk.client.suite;

import io.crnk.client.CrnkClient;
import io.crnk.client.TransportException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ScheduleRepository;
import io.crnk.test.suite.ExceptionAccessTestBase;
import org.junit.Assert;
import org.junit.Test;

public class ExceptionTest extends ExceptionAccessTestBase {

	public ExceptionTest() {
		ClientTestContainer testContainer = new ClientTestContainer();
		this.testContainer = testContainer;
	}

	@Test
	public void testInvalidUrl() {
		CrnkClient client = new CrnkClient("http://127.0.0.1:23423");
		taskRepo = client.getRepositoryForType(Task.class);
		try {
			taskRepo.findAll(new QuerySpec(Task.class));
			Assert.fail();
		} catch (TransportException e) {
			// ok
		}
	}

	@Test
	public void repoWithProxyAndInterface() {
		ScheduleRepository repo = ((ClientTestContainer) testContainer).getClient().getRepositoryForInterface(ScheduleRepository.class);

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