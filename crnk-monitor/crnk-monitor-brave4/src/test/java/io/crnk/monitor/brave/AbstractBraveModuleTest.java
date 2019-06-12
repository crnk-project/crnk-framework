package io.crnk.monitor.brave;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import brave.Tracing;
import io.crnk.client.CrnkClient;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.monitor.brave.mock.models.Project;
import io.crnk.monitor.brave.mock.models.Task;
import io.crnk.monitor.brave.mock.repository.ProjectRepository;
import io.crnk.monitor.brave.mock.repository.TaskRepository;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.JerseyTestBase;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

public abstract class AbstractBraveModuleTest extends JerseyTestBase {

	protected CrnkClient client;

	protected ResourceRepository<Task, Long> taskRepo;

	private Reporter<Span> clientReporter;

	private Reporter<Span> serverReporter;

	private HttpAdapter httpAdapter;

	private boolean isOkHttp;


	public AbstractBraveModuleTest(HttpAdapter httpAdapter) {
		this.httpAdapter = httpAdapter;
		this.isOkHttp = httpAdapter instanceof OkHttpAdapter;
	}


	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		Endpoint localEndpoint = Endpoint.newBuilder().serviceName("testClient").build();

		clientReporter = Mockito.mock(Reporter.class);
		Tracing clientTracing = Tracing.newBuilder()
				.spanReporter(clientReporter)
				.localEndpoint(localEndpoint)
				.build();

		client = new CrnkClient(getBaseUri().toString());
		client.setHttpAdapter(httpAdapter);
		client.addModule(BraveClientModule.create(clientTracing));
		taskRepo = client.getRepositoryForType(Task.class);
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
		Mockito.verify(clientReporter, Mockito.times(1)).report(clientSpanCaptor.capture());
		List<Span> clientSpans = clientSpanCaptor.getAllValues();
		Span callSpan = clientSpans.get(0);
		Assert.assertEquals("post", callSpan.name());
		Assert.assertEquals(Span.Kind.CLIENT, callSpan.kind());

		// check server local span
		ArgumentCaptor<Span> serverSpanCaptor = ArgumentCaptor.forClass(Span.class);

		// will resolve resource + relationship
		Mockito.verify(serverReporter, Mockito.times(1)).report(serverSpanCaptor.capture());
		List<Span> serverSpans = serverSpanCaptor.getAllValues();
		Span repositorySpan = serverSpans.get(0);
		Assert.assertEquals("crnk:post:/tasks/13/", repositorySpan.name());
		Assert.assertTrue(repositorySpan.toString().contains("\"lc\""));
		assertTag(repositorySpan, "lc", "crnk");
		assertTag(repositorySpan, "crnk.query", "?");

	}

	@Test
	public void testError() {
		Task task = new Task();
		task.setId(13L);
		try {
			taskRepo.create(task);
		}
		catch (Exception e) {
			// ok
		}

		// check client call and link span
		ArgumentCaptor<Span> clientSpanCaptor = ArgumentCaptor.forClass(Span.class);
		Mockito.verify(clientReporter, Mockito.times(1)).report(clientSpanCaptor.capture());
		List<Span> clientSpans = clientSpanCaptor.getAllValues();
		Span callSpan = clientSpans.get(0);
		Assert.assertEquals("post", callSpan.name());
		Assert.assertEquals(Span.Kind.CLIENT, callSpan.kind());
		assertTag(callSpan, "http.status_code", "500");

		// check server local span
		ArgumentCaptor<Span> serverSpanCaptor = ArgumentCaptor.forClass(Span.class);
		Mockito.verify(serverReporter, Mockito.times(1)).report(serverSpanCaptor.capture());
		List<Span> serverSpans = serverSpanCaptor.getAllValues();
		Span repositorySpan = serverSpans.get(0);
		Assert.assertEquals("crnk:post:/tasks/13/", repositorySpan.name());
		Assert.assertTrue(repositorySpan.toString().contains("\"lc\""));

		assertTag(repositorySpan, "lc", "crnk");
		assertTag(repositorySpan, "crnk.query", "?");
		assertTag(repositorySpan, "crnk.status", "EXCEPTION");
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
		Mockito.verify(clientReporter, Mockito.times(isOkHttp ? 1 : 1)).report(clientSpanCaptor.capture());
		List<Span> clientSpans = clientSpanCaptor.getAllValues();
		Span callSpan = clientSpans.get(0);
		Assert.assertEquals("get", callSpan.name());
		Assert.assertEquals(Span.Kind.CLIENT, callSpan.kind());

		// check server local span
		ArgumentCaptor<Span> serverSpanCaptor = ArgumentCaptor.forClass(Span.class);
		Mockito.verify(serverReporter, Mockito.times(1)).report(serverSpanCaptor.capture());
		List<Span> serverSpans = serverSpanCaptor.getAllValues();
		Span repositorySpan = serverSpans.get(0);
		Assert.assertEquals("crnk:get:/tasks/", repositorySpan.name());
		Assert.assertTrue(repositorySpan.toString().contains("\"lc\""));

		assertTag(repositorySpan, "lc", "crnk");
		assertTag(repositorySpan, "crnk.query", "?filter[name]=doe");
		assertTag(repositorySpan, "crnk.results", "0");
		assertTag(repositorySpan, "crnk.status", "OK");
	}

	@Test
	public void testFindTargets() {
		RelationshipRepository<Project, Serializable, Task, Serializable> relRepo = client
				.getRepositoryForType(Project.class, Task.class);
		relRepo.findManyTargets(123L, "tasks", new QuerySpec(Task.class));

		// check client call and link span
		ArgumentCaptor<Span> clientSpanCaptor = ArgumentCaptor.forClass(Span.class);
		Mockito.verify(clientReporter, Mockito.times(1)).report(clientSpanCaptor.capture());
		List<Span> clientSpans = clientSpanCaptor.getAllValues();
		Span callSpan = clientSpans.get(0);
		Assert.assertEquals("get", callSpan.name());
		Assert.assertEquals(Span.Kind.CLIENT, callSpan.kind());

		// check server local span
		ArgumentCaptor<Span> serverSpanCaptor = ArgumentCaptor.forClass(Span.class);
		Mockito.verify(serverReporter, Mockito.times(2)).report(serverSpanCaptor.capture());
		List<Span> serverSpans = serverSpanCaptor.getAllValues();

		Span repositorySpan0 = serverSpans.get(0);
		Assert.assertEquals("crnk:get:/tasks/", repositorySpan0.name());
		Assert.assertTrue(repositorySpan0.toString().contains("\"lc\""));

		assertTag(repositorySpan0, "lc", "crnk");
		assertTag(repositorySpan0, "crnk.results", "0");
		assertTag(repositorySpan0, "crnk.status", "OK");

		Span repositorySpan1 = serverSpans.get(1);
		Assert.assertEquals("crnk:get:/projects/123/tasks/", repositorySpan1.name());
		Assert.assertTrue(repositorySpan1.toString().contains("\"lc\""));

		assertTag(repositorySpan1, "lc", "crnk");
		assertTag(repositorySpan1, "crnk.query", "?");
		assertTag(repositorySpan1, "crnk.results", "0");
		assertTag(repositorySpan1, "crnk.status", "OK");
	}

	private void assertTag(Span span, String name, String value) {
		for (Map.Entry<String, String> entry : span.tags().entrySet()) {
			if (entry.getKey().equals(name)) {
				if (value != null) {
					Assert.assertEquals(value, entry.getValue());
				}
				return;
			}
		}
		Assert.fail(name + " not found");
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@ApplicationPath("/")
	private class TestApplication extends ResourceConfig {

		@SuppressWarnings("unchecked")
		public TestApplication() {
			property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, getClass().getPackage().getName());
			property(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");

			serverReporter = Mockito.mock(Reporter.class);

			Tracing tracing = Tracing.newBuilder()
					.localServiceName("testServer")
					.spanReporter(serverReporter)
					.build();

			CrnkFeature feature = new CrnkFeature();
			feature.addModule(BraveServerModule.create(tracing));
			register(feature);
		}
	}
}
