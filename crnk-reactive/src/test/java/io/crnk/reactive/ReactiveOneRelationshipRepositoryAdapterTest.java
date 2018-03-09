package io.crnk.reactive;

import java.util.Arrays;
import java.util.Map;

import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.reactive.internal.adapter.ReactiveOneRelationshipRepositoryAdapter;
import io.crnk.reactive.model.ReactiveProject;
import io.crnk.reactive.model.ReactiveTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReactiveOneRelationshipRepositoryAdapterTest extends ReactiveTestBase {

	private QuerySpec querySpec;

	private QuerySpecAdapter queryAdapter;

	private ReactiveOneRelationshipRepositoryAdapter adapter;

	@Before
	public void setup() {
		super.setup();

		querySpec = new QuerySpec(ReactiveTask.class);
		queryAdapter = new QuerySpecAdapter(querySpec, boot.getResourceRegistry(), queryContext);

		ResourceRegistry resourceRegistry = boot.getResourceRegistry();
		RegistryEntry entry = resourceRegistry.getEntry(ReactiveTask.class);
		adapter = (ReactiveOneRelationshipRepositoryAdapter) entry.getRelationshipRepository("project", null);
	}

	@Test
	public void setRelation() {
		ReactiveTask task = createTask(1);
		ReactiveProject project = createProject(2);
		adapter.setRelation(task, project.getId(), adapter.getResourceField(), queryAdapter).get();

		Map<Long, Long> relationMap = taskToProject.getRelationMap();
		Assert.assertEquals(1, relationMap.size());
		Assert.assertEquals(Long.valueOf(2L), relationMap.get(Long.valueOf(1L)));
	}


	@Test
	public void findOneTarget() {
		ReactiveProject project = createProject(2);
		projectRepository.getMap().put(2L, project);
		taskToProject.getRelationMap().put(1L, 2L);

		JsonApiResponse response = adapter.findOneTarget(1L, adapter.getResourceField(), queryAdapter).get();
		Assert.assertEquals(project, response.getEntity());
	}


	@Test
	public void findBulkOneTargets() {
		ReactiveProject project = createProject(2);
		projectRepository.getMap().put(2L, project);
		taskToProject.getRelationMap().put(1L, 2L);

		Map<Object, JsonApiResponse> responses =
				adapter.findBulkOneTargets(Arrays.asList(1L), adapter.getResourceField(), queryAdapter).get();
		Assert.assertEquals(1, responses.size());
		Assert.assertEquals(project, responses.get(1L).getEntity());
	}

}
