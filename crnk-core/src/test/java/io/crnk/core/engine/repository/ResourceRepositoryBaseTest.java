package io.crnk.core.engine.repository;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.CustomResourceRegistryTest;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.module.discovery.TestServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import org.junit.Before;
import org.junit.Test;

public class ResourceRepositoryBaseTest {

	private TestRepository repository;

	class TestRepository extends ResourceRepositoryBase<Task, Integer> {

		TestRepository() {
			super(Task.class);
		}

		@Override
		public ResourceList<Task> findAll(QuerySpec querySpec) {
			return new DefaultResourceList<>();
		}
	}

	@Before
	public void setup() {
		CoreTestContainer container = new CoreTestContainer();
		container.setDefaultPackage();
		container.getBoot().setPropertiesProvider(new PropertiesProvider() {
			@Override
			public String getProperty(String key) {
				if (key.equals(CrnkProperties.RETURN_404_ON_NULL)) {
					return "true";
				}
				return null;
			}
		});
		container.boot();


		repository = new TestRepository();
		repository.setResourceRegistry(container.getResourceRegistry());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void checkThrowExceptionWhenResourceNotFound() {
		repository.findOne(-1, new QuerySpec(Task.class));
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
