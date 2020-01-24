package io.crnk.test.suite;

import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.UnknownResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class ExceptionAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepository<Task, Long> taskRepo;

	@Before
	public void setup() {
		testContainer.start();
		taskRepo = testContainer.getRepositoryForType(Task.class);
	}

	@After
	public void tearDown() {
		testContainer.stop();
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

		ResourceRepository<UnknownResource, String> taskRepo = testContainer.getRepositoryForType(UnknownResource.class);

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
			testContainer.getRepositoryForType(String.class);
			Assert.fail();
		} catch (InvalidResourceException e) {
			// ok
		}
	}
}