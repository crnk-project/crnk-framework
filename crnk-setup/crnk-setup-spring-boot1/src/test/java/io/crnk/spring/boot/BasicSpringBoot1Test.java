package io.crnk.spring.boot;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import jakarta.security.auth.message.config.AuthConfigFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.internal.jackson.JacksonModule;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.spring.app.BasicSpringBootApplication;
import io.crnk.spring.mvc.SpringMvcModule;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.TaskRepository;
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert;
import org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BasicSpringBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class BasicSpringBoot1Test {

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private CrnkBoot boot;


	@Autowired
	private CrnkUiProperties uiProperties;

	@Autowired
	private CrnkHomeProperties homeProperties;

	@Autowired
	private ObjectProvider<CrnkSecurityProperties> securityProperties;

	@Autowired
	private CrnkSpringBootProperties coreProperties;

	@Autowired
	private CrnkOperationsProperties operationsProperties;

	@Autowired
	private CrnkValidationProperties validationProperties;

	@Autowired
	private CrnkMetaProperties metaProperties;

	@Autowired
	private MetaModuleConfigurer metaConfigurer;

	@Autowired
	private JpaModuleConfigurer jpaConfigurer;

	@Autowired
	private SpringMvcModule mvcModule;

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
	public void testProperties() {
		Assert.assertTrue(uiProperties.getEnabled());
		Assert.assertTrue(homeProperties.getEnabled());
		Assert.assertTrue(securityProperties.getIfAvailable() == null);
		Assert.assertTrue(coreProperties.isEnabled());
		Assert.assertTrue(operationsProperties.getEnabled());
		Assert.assertTrue(validationProperties.getEnabled());
		Assert.assertTrue(uiProperties.getEnabled());
		Assert.assertTrue(metaProperties.getEnabled());

		Mockito.verify(metaConfigurer, Mockito.times(1)).configure(Mockito.any(MetaModuleConfig.class));
		Mockito.verify(jpaConfigurer, Mockito.times(1)).configure(Mockito.any(JpaModuleConfig.class));

		Assert.assertEquals("spring.mvc", mvcModule.getModuleName());

		CrnkSecurityProperties unmanagedSecurityProperties = new CrnkSecurityProperties();
		Assert.assertTrue(unmanagedSecurityProperties.getEnabled());
		unmanagedSecurityProperties.setEnabled(false);
		Assert.assertFalse(unmanagedSecurityProperties.getEnabled());
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
				.getForEntity("http://localhost:" + this.port + "/api/tasks?include[tasks]=schedule,project", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		JsonFluentAssert included = assertThatJson(response.getBody()).node("included");
		included.isArray().ofLength(1);
	}


	@Test
	public void testJpa() {
		RestTemplate testRestTemplate = new RestTemplate();
		ResponseEntity<String> response = testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/api/building", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertThatJson(response.getBody()).node("data").isPresent();
	}

	@Test
	public void testPagingBehaviorInjected() {
		Assert.assertEquals(1, boot.getPagingBehaviors().size());
		Assert.assertTrue(boot.getPagingBehaviors().get(0) instanceof OffsetLimitPagingBehavior);
	}

	@Test
	@Ignore // currently not maintained
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