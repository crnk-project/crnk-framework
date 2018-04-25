package io.crnk.spring.boot;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.queryspec.QuerySpecDeserializer;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.spring.app.BasicSpringBoot2Application;

import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.TaskRepository;
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert;
import org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import javax.security.auth.message.config.AuthConfigFactory;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BasicSpringBoot2Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class BasicSpringBoot2Test {

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private QuerySpecDeserializer deserializer;

	@Autowired
	private CrnkBoot boot;


	@Before
	public void setup() {
		TestModule.clear();

		// NPE fix
		if (AuthConfigFactory.getFactory() == null) {
			AuthConfigFactory.setFactory(new AuthConfigFactoryImpl());
		}
	}

	@After
	public void tearDown() {
		TestModule.clear();
	}

	@Test
	public void testTestEndpointWithQueryParams() {
		RestTemplate testRestTemplate = new RestTemplate();
		ResponseEntity<String> response = testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/api/tasks?filter[tasks][name]=John", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertThatJson(response.getBody()).node("data").isPresent();
	}

	@Test
	public void testRelationshipInclusion() {
		Project project = new Project();
		ProjectRepository projectRepository = new ProjectRepository();
		projectRepository.save(project);

		Task task = new Task();
		task.setProject(project);
		TaskRepository taskRepository = new TaskRepository();
		taskRepository.save(task);

		RestTemplate testRestTemplate = new RestTemplate();
		ResponseEntity<String> response = testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/api/tasks?include[tasks]=schedule%2Cproject", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		JsonFluentAssert included = assertThatJson(response.getBody()).node("included");
		included.isArray().ofLength(1);
	}


	@Test
	public void testJpa() {
		RestTemplate testRestTemplate = new RestTemplate();
		ResponseEntity<String> response = testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/api/schedule", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertThatJson(response.getBody()).node("data").isPresent();
	}

	@Test
	public void testDeserializerInjected() {
		Assert.assertSame(boot.getQuerySpecDeserializer(), deserializer);
	}

	@Test
	public void testPagingBehaviorInjected() {
		Assert.assertEquals(1, boot.getPagingBehaviors().size());
		Assert.assertTrue(boot.getPagingBehaviors().get(0) instanceof OffsetLimitPagingBehavior);
	}

	@Test
	public void testUiModuleRunning() {
		RestTemplate testRestTemplate = new RestTemplate();
		ResponseEntity<String> response = testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/api/browse/index.html", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void testNonApiPathIsIgnored() {
		RestTemplate testRestTemplate = new RestTemplate();
		try {
			testRestTemplate
					.getForEntity("http://localhost:" + this.port + "/tasks", String.class);
			Assert.fail();
		} catch (HttpStatusCodeException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
		}
	}

	@Test
	public void testTestCustomEndpoint() {
		RestTemplate testRestTemplate = new RestTemplate();
		ResponseEntity<String> response = testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/api/custom", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(response.getBody(), "hello");
	}

	@Test
	public void testErrorsSerializedAsJsonApi() throws IOException {
		RestTemplate testRestTemplate = new RestTemplate();
		try {
			testRestTemplate
					.getForEntity("http://localhost:" + this.port + "/doesNotExist", String.class);
			Assert.fail();
		} catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());

			String body = e.getResponseBodyAsString();
			ObjectMapper mapper = new ObjectMapper();
			mapper.registerModule(JacksonModule.createJacksonModule());
			Document document = mapper.readerFor(Document.class).readValue(body);

			Assert.assertEquals(1, document.getErrors().size());
			ErrorData errorData = document.getErrors().get(0);
			Assert.assertEquals("404", errorData.getStatus());
			Assert.assertEquals("Not Found", errorData.getTitle());
			Assert.assertEquals("No message available", errorData.getDetail());
		}
	}
}