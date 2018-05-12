package io.crnk.servlet.reactive.model;

import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class SlowResourceRepository extends ResourceRepositoryBase<SlowTask, Long> implements HttpRequestContextAware {


	private Map<Long, SlowTask> resources = new HashMap<>();

	private HttpRequestContextProvider requestContextProvider;

	private int delay = 100;

	public SlowResourceRepository() {
		super(SlowTask.class);

		SlowTask task = new SlowTask();
		task.setId(1L);
		task.setName("task1");
		resources.put(task.getId(), task);
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	@Override
	public ResourceList<SlowTask> findAll(QuerySpec querySpec) {
		try {
			Assert.assertNotNull(requestContextProvider);
			Assert.assertTrue(requestContextProvider.hasThreadRequestContext());
			Assert.assertNotNull(requestContextProvider.getRequestContext());
			Assert.assertNotNull(requestContextProvider.getRequestContext().getQueryContext());

			Thread.sleep(delay);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
		return querySpec.apply(resources.values());
	}

	public Map<Long, SlowTask> getMap() {
		return resources;
	}

	public void clear() {
		resources.clear();
	}

	@Override
	public void setHttpRequestContextProvider(HttpRequestContextProvider requestContextProvider) {
		this.requestContextProvider = requestContextProvider;
	}
}
