package io.crnk.core.repository;

import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class InMemoryResourceRepositoryTest {

	@Test
	public void testStandaloneUse() {
		Task task = new Task();
		task.setId(131L);
		InMemoryResourceRepository<Task, String> repository = new InMemoryResourceRepository<>(Task.class);
		repository.create(task);

		ResourceList<Task> list = repository.findAll(new QuerySpec(Task.class));
		Assert.assertEquals(list.size(), 1);
	}
}
