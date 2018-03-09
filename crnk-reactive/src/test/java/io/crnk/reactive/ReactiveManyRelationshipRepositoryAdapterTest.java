package io.crnk.reactive;

import java.util.Arrays;
import java.util.Map;

import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.reactive.internal.adapter.ReactiveManyRelationshipRepositoryAdapter;
import io.crnk.reactive.model.ReactiveProject;
import io.crnk.reactive.model.ReactiveTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReactiveManyRelationshipRepositoryAdapterTest extends ReactiveTestBase {

	private QuerySpec querySpec;

	private QuerySpecAdapter queryAdapter;

	private ReactiveManyRelationshipRepositoryAdapter adapter;

	@Before
	public void setup() {
		super.setup();

		querySpec = new QuerySpec(ReactiveTask.class);
		queryAdapter = new QuerySpecAdapter(querySpec, boot.getResourceRegistry(), queryContext);

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RegistryEntry entry = resourceRegistry.getEntry(ReactiveProject.class);
		adapter = (ReactiveManyRelationshipRepositoryAdapter) entry.getRelationshipRepository("tasks", null);
	}

	@Test
	public void setRelations() {
		ReactiveTask task = createTask(1);
		ReactiveProject project = createProject(2);
		adapter.setRelations(project, Arrays.asList(task.getId()), adapter.getResourceField(), queryAdapter).get();

		MultivaluedMap<Long, Long> relationMap = projectToTasks.getRelationMap();
		Assert.assertEquals(1, relationMap.keySet().size());
		Assert.assertEquals(Arrays.asList(Long.valueOf(task.getId())), relationMap.getList(Long.valueOf(project.getId())));
	}

	@Test
	public void addRemoveRelations() {
		ReactiveTask task1 = createTask(1);
		ReactiveTask task2 = createTask(2);
		ReactiveTask task3 = createTask(3);
		ReactiveProject project = createProject(2);
		adapter.addRelations(project, Arrays.asList(task1.getId()), adapter.getResourceField(), queryAdapter).get();
		adapter.addRelations(project, Arrays.asList(task2.getId()), adapter.getResourceField(), queryAdapter).get();
		adapter.addRelations(project, Arrays.asList(task3.getId()), adapter.getResourceField(), queryAdapter).get();
		adapter.removeRelations(project, Arrays.asList(task2.getId()), adapter.getResourceField(), queryAdapter).get();

		MultivaluedMap<Long, Long> relationMap = projectToTasks.getRelationMap();
		Assert.assertEquals(1, relationMap.keySet().size());
		Assert.assertEquals(Arrays.asList(task1.getId(), task3.getId()), relationMap.getList(Long.valueOf(project.getId())));
	}

	@Test
	public void findManyTarget() {
		ReactiveTask task2 = createTask(2);
		ReactiveTask task3 = createTask(3);
		taskRepository.getMap().put(2L, task2);
		taskRepository.getMap().put(3L, task3);
		projectToTasks.getRelationMap().addAll(1L, Arrays.asList(2L, 3L));

		JsonApiResponse response = adapter.findManyTargets(1L, adapter.getResourceField(), queryAdapter).get();
		Assert.assertEquals(Arrays.asList(task2, task3), response.getEntity());
	}

	@Test
	public void findBulkManyTargets() {
		ReactiveTask task2 = createTask(2);
		ReactiveTask task3 = createTask(3);
		taskRepository.getMap().put(2L, task2);
		taskRepository.getMap().put(3L, task3);
		projectToTasks.getRelationMap().addAll(1L, Arrays.asList(2L, 3L));

		Map<Object, JsonApiResponse> responses = adapter.findBulkManyTargets(Arrays.asList(1L), adapter.getResourceField(), queryAdapter).get();
		Assert.assertEquals(1, responses.keySet().size());
		Assert.assertEquals(Arrays.asList(task2, task3), responses.get(1L).getEntity());
	}

}
