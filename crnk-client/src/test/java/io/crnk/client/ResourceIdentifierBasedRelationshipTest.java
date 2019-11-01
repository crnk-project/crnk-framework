package io.crnk.client;

import java.util.Arrays;
import java.util.Map;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.ResourceIdentifierBasedRelationshipResource;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Test;

/**
 * Make use of ResourceIdentifier with @JsonRelationId. Brings the benefit of having both type information and id within ResourceIdentifier.
 */
public class ResourceIdentifierBasedRelationshipTest extends AbstractClientTest {

	@Test
	public void test() {
		ResourceRepository<Task, Object> tasks = client.getRepositoryForType(Task.class);
		Task task = new Task();
		task.setId(1L);
		task.setName("someTask");
		tasks.create(task);

		ResourceRepository<ResourceIdentifierBasedRelationshipResource, Object> repository = client.getRepositoryForType(ResourceIdentifierBasedRelationshipResource.class);
		ResourceIdentifierBasedRelationshipResource resource = new ResourceIdentifierBasedRelationshipResource();
		resource.setId(2L);
		resource.setTaskId(new ResourceIdentifier("1", "tasks"));
		resource.setTaskIds(Arrays.asList(new ResourceIdentifier("1", "tasks")));
		repository.create(resource);

		QuerySpec querySpec = new QuerySpec(ResourceIdentifierBasedRelationshipResource.class);
		querySpec.includeRelation(PathSpec.of("task"));
		ResourceList<ResourceIdentifierBasedRelationshipResource> list = repository.findAll(querySpec);
		Assert.assertEquals(1, list.size());
		resource = list.get(0);
		Assert.assertEquals(2L, resource.getId());
		Assert.assertEquals(new ResourceIdentifier("1", "tasks"), resource.getTaskId());
		Assert.assertNotNull(resource.getTask());
		Assert.assertEquals("someTask", resource.getTask().getName());

		OneRelationshipRepository<ResourceIdentifierBasedRelationshipResource, Object, Task, Object> oneRelationshipRepository =
				client.getOneRepositoryForType(ResourceIdentifierBasedRelationshipResource.class, Task.class);
		Map<Object, Task> relationships = oneRelationshipRepository.findOneRelations(Arrays.asList(2L), "task", new QuerySpec(Task.class));
		Assert.assertEquals(1, relationships.size());
		Assert.assertNotNull(relationships.get(2L));
		Assert.assertNotNull(relationships.get(2L) instanceof Task);

		ManyRelationshipRepository<ResourceIdentifierBasedRelationshipResource, Object, Task, Object> manyRelationshipRepository =
				client.getManyRepositoryForType(ResourceIdentifierBasedRelationshipResource.class, Task.class);
		Map<Object, ResourceList<Task>> manyRelationships = manyRelationshipRepository.findManyRelations(Arrays.asList(2L), "tasks", new QuerySpec(Task.class));
		Assert.assertEquals(1, manyRelationships.size());
		ResourceList<Task> manyList = manyRelationships.get(2L);
		Assert.assertEquals(1, manyList.size());
		Assert.assertNotNull(manyList.get(0) instanceof Task);
	}
}