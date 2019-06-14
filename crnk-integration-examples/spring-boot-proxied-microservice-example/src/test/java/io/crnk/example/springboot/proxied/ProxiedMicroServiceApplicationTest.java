package io.crnk.example.springboot.proxied;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import io.crnk.client.CrnkClient;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.proxied.microservice.ProxiedMicroServiceApplication;
import io.crnk.example.springboot.proxied.microservice.task.ProjectProxy;
import io.crnk.example.springboot.proxied.microservice.task.Task;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serializable;
import java.util.Arrays;

public class ProxiedMicroServiceApplicationTest {

	private ConfigurableApplicationContext projectApp;

	private ConfigurableApplicationContext taskApp;

	private CrnkClient taskClient;

	@Before
	public void setup() {
		projectApp = ProxiedMicroServiceApplication.startProjectApplication();
		taskApp = ProxiedMicroServiceApplication.startTaskApplication();

		String url = "http://127.0.0.1:" + ProxiedMicroServiceApplication.TASK_PORT;
		taskClient = new CrnkClient(url);
		RestAssured.baseURI = url;
	}

	@Test
	public void test() {
		checkInclusionOfRemoteResource();
		checkRemoteProjectNotExposedInHome();
		checkRemoteProjectNotExposed();
	}

	private void checkInclusionOfRemoteResource() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.setLimit(10L);
		querySpec.includeRelation(Arrays.asList("project"));

		ResourceRepository<Task, Serializable> repository = taskClient.getRepositoryForType(Task.class);
		ResourceList<Task> tasks = repository.findAll(querySpec);
		Assert.assertNotEquals(0, tasks.size());
		for (Task task : tasks) {
			Assert.assertEquals("http://127.0.0.1:12001/task/" + task.getId(), task.getLinks().getSelf());
			ProjectProxy project = task.getProject();

			Assert.assertNotNull(task.getProject());
			Assert.assertEquals("Great Project", project.getName());
			Assert.assertEquals(2, project.getAttributes().size());
			Assert.assertEquals("P1", project.getAttributes().get("programme").toString());
			Assert.assertEquals("Mike", project.getAttributes().get("owner").toString());
			Assert.assertEquals(2, project.getAttributes().size());

			Assert.assertEquals("http://127.0.0.1:12002/project/" + project.getId(), project.getLinks().getSelf());
		}
	}

	private void checkRemoteProjectNotExposedInHome() {
		Response response = RestAssured.given().when().get("/");
		response.then().assertThat().statusCode(200);
		String body = response.getBody().print();
		Assert.assertTrue(body, body.contains("/task"));
		Assert.assertTrue(body, !body.contains("/project"));
	}

	private void checkRemoteProjectNotExposed() {
		Response response = RestAssured.given().when().get("/project");
		response.then().assertThat().statusCode(404);
	}

	@After
	public void tearDown() {
		projectApp.close();
		taskApp.close();
	}
}
