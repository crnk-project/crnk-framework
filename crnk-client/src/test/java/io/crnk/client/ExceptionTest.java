package io.crnk.client;

import io.crnk.client.http.AbstractClientTest;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.UnknownResource;
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
	public void testUnknownExceptionMapping() {
		Task task = new Task();
		task.setId(10001L);
		task.setName("test");
		try {
			taskRepo.create(task);
			Assert.fail();
		} catch (InternalServerErrorException e) {
			// ok
		}
	}


	@Test
	public void testUnknownRepository() {
		UnknownResource task = new UnknownResource();
		task.setId("test");

		ResourceRepositoryV2<UnknownResource, String> taskRepo = client.getRepositoryForType(UnknownResource.class);

		try {
			taskRepo.create(task);
			Assert.fail();
		} catch (ResourceNotFoundException e) {
			// ok
		}
	}

	@Test
	public void testInvalidResource() {
		try {
			client.getRepositoryForType(String.class);
			Assert.fail();
		} catch (InvalidResourceException e) {
			// ok
		}
	}

	@Test
	public void testInvalidUrl() {
		client = new CrnkClient("http://127.0.0.1:23423");
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