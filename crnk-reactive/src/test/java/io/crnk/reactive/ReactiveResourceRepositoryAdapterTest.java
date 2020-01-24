package io.crnk.reactive;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.reactive.internal.adapter.ReactiveResourceRepositoryAdapter;
import io.crnk.reactive.model.ReactiveTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

public class ReactiveResourceRepositoryAdapterTest extends ReactiveTestBase {

	private QuerySpec querySpec;
	private QuerySpecAdapter queryAdapter;
	private ReactiveResourceRepositoryAdapter adapter;

	@Before
	public void setup() {
		super.setup();

		querySpec = new QuerySpec(ReactiveTask.class);
		QueryContext queryContext = new QueryContext();
		queryAdapter = new QuerySpecAdapter(querySpec, boot.getResourceRegistry(), queryContext);

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RegistryEntry entry = resourceRegistry.getEntry(ReactiveTask.class);
		adapter = (ReactiveResourceRepositoryAdapter) entry.getResourceRepository();
	}

	@Test
	public void checkRegistryRegistration() {
		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RegistryEntry entry = resourceRegistry.getEntry(ReactiveTask.class);

		Assert.assertNotNull(entry);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Assert.assertEquals("reactive/task", resourceInformation.getResourceType());
		ResourceRepositoryAdapter adapter = entry.getResourceRepository();
		Assert.assertEquals(ReactiveResourceRepositoryAdapter.class, adapter.getClass());
	}

	@Test
	public void checkCreate() {
		ReactiveTask task = createTask(1);
		Result<JsonApiResponse> result = adapter.create(task, queryAdapter);
		JsonApiResponse response = result.get();
		Assert.assertEquals(task, response.getEntity());

		Map<Long, ReactiveTask> map = taskRepository.getMap();
		Assert.assertEquals(task, map.get(1L));
	}

	@Test
	public void checkSave() {
		ReactiveTask task = createTask(1);
		Result<JsonApiResponse> result = adapter.update(task, queryAdapter);
		JsonApiResponse response = result.get();
		Assert.assertEquals(task, response.getEntity());

		Map<Long, ReactiveTask> map = taskRepository.getMap();
		Assert.assertEquals(task, map.get(1L));
	}

	@Test
	public void checkFindOne() {
		Map<Long, ReactiveTask> map = taskRepository.getMap();
		ReactiveTask task = createTask(1);
		map.put(1L, task);

		Result<JsonApiResponse> result = adapter.findOne(1L, queryAdapter);
		JsonApiResponse response = result.get();
		Assert.assertEquals(task, response.getEntity());
	}

	@Test
	public void checkFindAll() {
		Map<Long, ReactiveTask> map = taskRepository.getMap();
		ReactiveTask task = createTask(1);
		map.put(1L, task);

		Result<JsonApiResponse> result = adapter.findAll(queryAdapter);
		JsonApiResponse response = result.get();
		ResourceList list = (ResourceList) response.getEntity();
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(task, list.get(0));
	}

	@Test
	public void checkFindAllById() {
		Map<Long, ReactiveTask> map = taskRepository.getMap();
		ReactiveTask task = createTask(1);
		map.put(1L, task);

		Result<JsonApiResponse> result = adapter.findAll(Arrays.asList(1L), queryAdapter);
		JsonApiResponse response = result.get();
		ResourceList list = (ResourceList) response.getEntity();
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(task, list.get(0));
	}

	@Test
	public void checkDelete() {
		Map<Long, ReactiveTask> map = taskRepository.getMap();
		ReactiveTask task = createTask(1);
		map.put(1L, task);

		Result<JsonApiResponse> result = adapter.delete(1L, queryAdapter);
		result.get();
		Assert.assertTrue(map.isEmpty());
	}
}
