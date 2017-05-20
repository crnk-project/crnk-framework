package io.crnk.client;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterListener;
import io.crnk.client.module.HttpAdapterAware;
import io.crnk.core.module.Module;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.test.mock.models.Task;
import okhttp3.OkHttpClient.Builder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ModuleTest extends AbstractClientTest {

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	private TestOkHttpAdapterListener adapterListener = Mockito.spy(new TestOkHttpAdapterListener());

	private OkHttpTestModule testModule = Mockito.spy(new OkHttpTestModule());

	@Before
	public void setup() {
		super.setup();
		client.addModule(testModule);
		taskRepo = client.getQuerySpecRepository(Task.class);
	}

	@Override
	protected TestApplication configure() {
		return new TestApplication(true);
	}

	@Test
	public void test() {
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
			// nothing to do
		}

		@Override
		public void setHttpAdapter(HttpAdapter adapter) {
			((OkHttpAdapter) adapter).addListener(adapterListener);
		}

	}

}