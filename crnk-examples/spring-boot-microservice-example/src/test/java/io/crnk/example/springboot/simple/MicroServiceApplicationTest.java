package io.crnk.example.springboot.simple;

import java.io.Serializable;
import java.util.Arrays;

import io.crnk.client.CrnkClient;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.microservice.MicroServiceApplication;
import io.crnk.example.springboot.microservice.project.Project;
import io.crnk.example.springboot.microservice.task.Task;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

public class MicroServiceApplicationTest {

	private ConfigurableApplicationContext projectApp;

	private ConfigurableApplicationContext taskApp;

	private CrnkClient taskClient;

	@Before
	public void setup() {
		projectApp = MicroServiceApplication.startProjectApplication();
		taskApp = MicroServiceApplication.startTaskApplication();

		taskClient = new CrnkClient("http://127.0.0.1:" + MicroServiceApplication.TASK_PORT);
	}

	@Test
	public void test() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.setLimit(10L);
		querySpec.includeRelation(Arrays.asList("project"));

		ResourceRepositoryV2<Task, Serializable> repository = taskClient.getRepositoryForType(Task.class);
		ResourceList<Task> tasks = repository.findAll(querySpec);
		Assert.assertNotEquals(0, tasks.size());
		for (Task task : tasks) {
			Assert.assertEquals("http://127.0.0.1:12001/task/" + task.getId(), task.getLinks().getSelf());
			Project project = task.getProject();
			Assert.assertNotNull(task.getProject());
			Assert.assertEquals("http://127.0.0.1:12002/project/" + project.getId(), project.getLinks().getSelf());
		}
	}

	@After
	public void tearDown() {
		projectApp.close();
		taskApp.close();
	}
}
