package io.crnk.spring.cloud.sleuth;

import io.crnk.client.CrnkClient;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.spring.app.TestSpanReporter;
import io.crnk.spring.client.RestTemplateAdapter;
import io.crnk.test.JerseyTestBase;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.TaskRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.Span;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public abstract class SleuthModuleTest extends JerseyTestBase {

	protected CrnkClient client;

	protected ResourceRepository<Task, Long> taskRepo;

	@Autowired
	private TestSpanReporter reportedSpans;

	private ResourceRepository<Project, Serializable> projectRepo;


	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		RestTemplateAdapter httpAdapter = new RestTemplateAdapter();
		client = new CrnkClient(getBaseUri().toString());
		client.setHttpAdapter(httpAdapter);
		taskRepo = client.getRepositoryForType(Task.class);
		projectRepo = client.getRepositoryForType(Project.class);
		TaskRepository.clear();
		ProjectRepository.clear();
		httpAdapter.setReceiveTimeout(10000, TimeUnit.SECONDS);
	}

	@Test
	public void testCreate() {
		Task task = new Task();
		task.setId(13L);
		task.setName("myTask");
		taskRepo.create(task);

		// check client call and link span
		ArgumentCaptor<Span> clientSpanCaptor = ArgumentCaptor.forClass(Span.class);
		List<Span> clientSpans = clientSpanCaptor.getAllValues();
		Span callSpan = clientSpans.get(0);
		Assert.assertEquals("post", callSpan.getName());
		Assert.assertTrue(callSpan.toString().contains("\"cs\""));
		Assert.assertTrue(callSpan.toString().contains("\"cr\""));

		// check server local span
		Assert.assertEquals(1, reportedSpans.spans.size());
		Span repositorySpan = reportedSpans.spans.get(0);
		Assert.assertEquals("crnk:post:/tasks/13", repositorySpan.getName());
		Assert.assertTrue(repositorySpan.toString().contains("\"lc\""));

		assertBinaryAnnotation(repositorySpan, "lc", "crnk");
		assertBinaryAnnotation(repositorySpan, "crnk.query", "?");
	}

	@Test
	public void testError() {
		Task task = new Task();
		task.setId(13L);
		try {
			taskRepo.create(task);
		} catch (Exception e) {
			// ok
		}

		// check client call and link span
		ArgumentCaptor<Span> clientSpanCaptor = ArgumentCaptor.forClass(Span.class);
		List<Span> clientSpans = clientSpanCaptor.getAllValues();
		Span callSpan = clientSpans.get(0);
		Assert.assertEquals("post", callSpan.getName());
		Assert.assertTrue(callSpan.toString().contains("\"cs\""));
		Assert.assertTrue(callSpan.toString().contains("\"cr\""));
		assertBinaryAnnotation(callSpan, "http.status_code", "500");

		// check server local span
		Assert.assertEquals(1, reportedSpans.spans.size());
		Span repositorySpan = reportedSpans.spans.get(0);
		Assert.assertEquals("crnk:post:/tasks/13", repositorySpan.getName());
		Assert.assertTrue(repositorySpan.toString().contains("\"lc\""));

		assertBinaryAnnotation(repositorySpan, "lc", "crnk");
		assertBinaryAnnotation(repositorySpan, "crnk.query", "?");
		assertBinaryAnnotation(repositorySpan, "crnk.status", "EXCEPTION");
	}

	@Test
	public void testFindAll() {
		Task task = new Task();
		task.setId(13L);
		task.setName("myTask");
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "doe"));
		taskRepo.findAll(querySpec);

		// check client call and link span
		ArgumentCaptor<Span> clientSpanCaptor = ArgumentCaptor.forClass(Span.class);
		List<Span> clientSpans = clientSpanCaptor.getAllValues();
		Span callSpan = clientSpans.get(0);
		Assert.assertEquals("get", callSpan.getName());
		Assert.assertTrue(callSpan.toString().contains("\"cs\""));
		Assert.assertTrue(callSpan.toString().contains("\"cr\""));

		// check server local span
		Assert.assertEquals(1, reportedSpans.spans.size());
		Span repositorySpan = reportedSpans.spans.get(0);
		Assert.assertEquals("crnk:get:/tasks", repositorySpan.getName());
		Assert.assertTrue(repositorySpan.toString().contains("\"lc\""));

		assertBinaryAnnotation(repositorySpan, "lc", "crnk");
		assertBinaryAnnotation(repositorySpan, "crnk.query", "?filter[tasks][name][EQ]=doe");
		assertBinaryAnnotation(repositorySpan, "crnk.results", "0");
		assertBinaryAnnotation(repositorySpan, "crnk.status", "OK");
	}

	@Test
	public void testFindTargets() {
		RelationshipRepositoryV2<Project, Serializable, Task, Serializable> relRepo = client
				.getRepositoryForType(Project.class, Task.class);
		relRepo.findManyTargets(123L, "tasks", new QuerySpec(Task.class));

		// check client call and link span
		ArgumentCaptor<Span> clientSpanCaptor = ArgumentCaptor.forClass(Span.class);
		List<Span> clientSpans = clientSpanCaptor.getAllValues();
		Span callSpan = clientSpans.get(0);
		Assert.assertEquals("get", callSpan.getName());
		Assert.assertTrue(callSpan.toString().contains("\"cs\""));
		Assert.assertTrue(callSpan.toString().contains("\"cr\""));

		// check server local span
		Assert.assertEquals(2, reportedSpans.spans.size());

		Span repositorySpan0 = reportedSpans.spans.get(0);
		Assert.assertEquals("crnk:get:/tasks", repositorySpan0.getName());
		Assert.assertTrue(repositorySpan0.toString().contains("\"lc\""));

		assertBinaryAnnotation(repositorySpan0, "lc", "crnk");
		assertBinaryAnnotation(repositorySpan0, "crnk.results", "0");
		assertBinaryAnnotation(repositorySpan0, "crnk.status", "OK");

		Span repositorySpan1 = reportedSpans.spans.get(1);
		Assert.assertEquals("crnk:get:/projects/123/tasks", repositorySpan1.getName());
		Assert.assertTrue(repositorySpan1.toString().contains("\"lc\""));

		assertBinaryAnnotation(repositorySpan1, "lc", "crnk");
		assertBinaryAnnotation(repositorySpan1, "crnk.query", "?");
		assertBinaryAnnotation(repositorySpan1, "crnk.results", "0");
		assertBinaryAnnotation(repositorySpan1, "crnk.status", "OK");
	}

	private void assertBinaryAnnotation(Span span, String key, String value) {
		Assert.assertEquals(value, span.tags().get(key));
	}
}
