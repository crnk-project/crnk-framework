package io.crnk.servlet.reactive;


import io.crnk.client.CrnkClient;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.servlet.resource.ReactiveServletTestApplication;
import io.crnk.servlet.resource.ReactiveServletTestContainer;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveServletTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Ignore
public class ReactiveStressTest {


	private ReactiveServletTestContainer testContainer;

	@Autowired
	public void setTestContainer(ReactiveServletTestContainer testContainer) {
		this.testContainer = testContainer;
	}

	@Test
	public void testResourceAccess() throws InterruptedException, ExecutionException {

		List<Future> futures = new ArrayList();

		ExecutorService executor = Executors.newFixedThreadPool(50);
		for (int i = 0; i < 1000; i++) {
			final long id = i;
			Future<?> future = executor.submit(() -> {

				String url = testContainer.getBaseUrl();
				CrnkClient client = new CrnkClient(url);

				ResourceRepository<Task, Serializable> taskRepo = client.getRepositoryForType(Task.class);

				Task task = new Task();
				task.setId(id + 100000);
				task.setName("test");
				taskRepo.create(task);

				for (int j = 0; j < 10; j++) {
					Task foundTask = taskRepo.findOne(task.getId(), new QuerySpec(Task.class));
					Assert.assertEquals(task.getId(), foundTask.getId());
				}
			});
			futures.add(future);
		}

		for (Future future : futures) {
			future.get();
		}
		executor.shutdown();
	}

	@Test
	public void testRelationAccess() throws InterruptedException, ExecutionException {
		List<Future> futures = new ArrayList();
		ExecutorService executor = Executors.newFixedThreadPool(50);
		for (int i = 0; i < 1000; i++) {
			final long id = i;
			Future<?> future = executor.submit(() -> {
				String url = testContainer.getBaseUrl();
				CrnkClient client = new CrnkClient(url);

				ResourceRepository<Schedule, Serializable> scheduleRepo = client.getRepositoryForType(Schedule.class);
				ResourceRepository<Task, Serializable> taskRepo = client.getRepositoryForType(Task.class);
				RelationshipRepositoryV2<Task, Serializable, Schedule, Serializable> relRepo = client.getRepositoryForType(Task.class, Schedule.class);


				Schedule schedule = new Schedule();
				schedule.setId(id);
				schedule.setName("schedule");
				scheduleRepo.create(schedule);

				Task task = new Task();
				task.setId(id + 100000);
				task.setName("test");
				taskRepo.create(task);

				relRepo.setRelation(task, schedule.getId(), "schedule");

				for (int j = 0; j < 10; j++) {
					Schedule relSchedule = relRepo.findOneTarget(task.getId(), "schedule", new QuerySpec(Schedule.class));
					Assert.assertNotNull(relSchedule);
					Assert.assertEquals(schedule.getId(), relSchedule.getId());
				}
			});
			futures.add(future);
		}

		for (Future future : futures) {
			future.get();
		}
		executor.shutdown();
	}

}
