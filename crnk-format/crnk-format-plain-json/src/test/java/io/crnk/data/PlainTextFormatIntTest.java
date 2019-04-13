package io.crnk.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.crnk.client.CrnkClient;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.format.plainjson.PlainJsonFormatModule;
import io.crnk.home.HomeModule;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.JerseyTestBase;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.TaskRepository;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PlainTextFormatIntTest extends JerseyTestBase {


	@Override
	protected Application configure() {
		return new TestApplication();

	}

	@Before
	public void setup() {
		ProjectRepository projectRepository = new ProjectRepository();
		Project project = new Project();
		project.setId(3L);
		project.setName("someProject");
		projectRepository.save(project);

		TaskRepository taskRepository = new TaskRepository();
		Task task = new Task();
		task.setName("someTask");
		task.setId(12L);
		task.setProject(project);
		taskRepository.save(task);
	}

	@After
	public void cleanup() {
		TestModule.clear();
	}

	@Test
	public void checkGet() {
		Response getResponse = RestAssured.get(getBaseUri() + "/tasks/12?include=project");
		Assert.assertEquals(200, getResponse.getStatusCode());
		getResponse.then().assertThat().body("data.id", Matchers.equalTo("12"));
		getResponse.then().assertThat().body("data.type", Matchers.equalTo("tasks"));
		getResponse.then().assertThat().body("data.name", Matchers.equalTo("someTask"));
		getResponse.then().assertThat().body("data.project.data.id", Matchers.equalTo("1"));
		getResponse.then().assertThat().body("data.project.data.type", Matchers.equalTo("projects"));
		getResponse.then().assertThat().body("data.project.data.links.value", Matchers.equalTo("someLinkValue"));
		getResponse.then().assertThat().body("data.project.links.self", Matchers.endsWith("/tasks/12/relationships/project"));
	}

	@Test
	public void checkJsonApiAccess() {
		CrnkClient client = new CrnkClient(getBaseUri().toString());
		ResourceRepository<Task, Serializable> repository = client.getRepositoryForType(Task.class);
		Task createdTask = repository.findOne(12L, new QuerySpec(Task.class));
		Assert.assertEquals("someTask", createdTask.getName());
	}

	@Test
	public void checkCrud() {
		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("id", "13");
		dataMap.put("type", "tasks");
		dataMap.put("name", "otherTask");

		Map<String, Object> documentMap = new HashMap<>();
		documentMap.put("data", dataMap);
		documentMap.put("lastName", "Doe");

		Response postResponse = RestAssured.given().
				contentType(HttpHeaders.JSON_CONTENT_TYPE).
				body(documentMap).
				when().
				post(getBaseUri() + "/tasks");
		postResponse.then().statusCode(201);


		CrnkClient client = new CrnkClient(getBaseUri().toString());
		ResourceRepository<Task, Serializable> repository = client.getRepositoryForType(Task.class);
		Task createdTask = repository.findOne(13L, new QuerySpec(Task.class));
		Assert.assertEquals("otherTask", createdTask.getName());
	}

	@ApplicationPath("/")
	private class TestApplication extends ResourceConfig {

		public TestApplication() {
			PlainJsonFormatModule module = new PlainJsonFormatModule();

			CrnkFeature feature = new CrnkFeature();
			feature.addModule(module);
			feature.addModule(new TestModule());
			feature.addModule(HomeModule.create());
			register(feature);
		}
	}
}
