package io.crnk.client.module;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.crnk.client.AbstractClientTest;
import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListener;
import io.crnk.core.module.Module;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.UnknownResource;
import okhttp3.OkHttpClient.Builder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ModuleClientTest extends AbstractClientTest {


	private TestOkHttpAdapterListener adapterListener = Mockito.spy(new TestOkHttpAdapterListener());

	private OkHttpTestModule testModule = Mockito.spy(new OkHttpTestModule());

	@Before
	public void setup() {
		super.setup();
		client.setHttpAdapter(new OkHttpAdapter());
		client.addModule(testModule);
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication();
	}

	@Test
	public void test() {
		ResourceRepositoryV2<Task, Long> taskRepo = client.getRepositoryForType(Task.class);
		Task task = new Task();
		task.setId(1L);
		task.setName("task");
		taskRepo.create(task);

		List<Task> tasks = taskRepo.findAll(new QuerySpec(Task.class));
		Assert.assertEquals(1, tasks.size());

		Mockito.verify(testModule, Mockito.times(1)).setupModule(Mockito.any(ModuleContext.class));
		Mockito.verify(testModule, Mockito.times(1)).setHttpAdapter(Mockito.eq(client.getHttpAdapter()));
		Mockito.verify(adapterListener, Mockito.times(1)).onBuild(Mockito.any(Builder.class));
	}

	@Test
	public void testResourceLookupInitializesRepository() {
		Assert.assertNotNull(client.getRegistry().findEntry(Schedule.class));

		// related loaded as well
		Assert.assertNotNull(client.getRegistry().findEntry(Project.class));
		Assert.assertNotNull(client.getRegistry().findEntry(Task.class));

		// unrelated not loaded
		Assert.assertNotNull(client.getRegistry().findEntry(UnknownResource.class));
	}

	@Test
	public void testReconfigureHttpAdapter() {
		OkHttpAdapter newAdapter = new OkHttpAdapter();
		client.setHttpAdapter(newAdapter);

		Mockito.verify(testModule, Mockito.times(1)).setHttpAdapter(newAdapter);
	}

	class TestOkHttpAdapterListener implements OkHttpAdapterListener {

		@Override
		public void onBuild(Builder builder) {
			builder.connectTimeout(10000, TimeUnit.MILLISECONDS);
		}
	}

	private class OkHttpTestModule implements Module, HttpAdapterAware {

		@Override
		public String getModuleName() {
			return "okhttp-test";
		}

		@Override
		public void setupModule(ModuleContext context) {
			context.addResourceLookup(new TestResourceLookup());


		}

		@Override
		public void setHttpAdapter(HttpAdapter adapter) {
			((OkHttpAdapter) adapter).addListener(adapterListener);
		}
	}

	private class TestResourceLookup implements ResourceLookup {

		@Override
		public Set<Class<?>> getResourceClasses() {
			Set<Class<?>> set = new HashSet<>();
			set.add(Schedule.class);
			return set;
		}
	}
}