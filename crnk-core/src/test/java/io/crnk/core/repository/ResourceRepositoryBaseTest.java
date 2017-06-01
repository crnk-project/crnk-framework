package io.crnk.core.repository;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.registry.ResourceRegistryBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class ResourceRepositoryBaseTest {

	private TestRepository repository;

	class TestRepository extends ResourceRepositoryBase<Task, Integer> {

		protected TestRepository() {
			super(Task.class);
		}

		@Override
		public ResourceList<Task> findAll(QuerySpec querySpec) {
			return new DefaultResourceList<>();
		}
	}

	@Before
	public void setup() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE));
		boot.boot();

		repository = new TestRepository();
		repository.setResourceRegistry(boot.getResourceRegistry());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void checkThrowExceptionWhenResourceNotFound() {
		repository.findOne(12, new QuerySpec(Task.class));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void saveNotSupported() {
		repository.save(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void createNotSupported() {
		repository.create(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void deleteNotSupported() {
		repository.delete(null);
	}
}
