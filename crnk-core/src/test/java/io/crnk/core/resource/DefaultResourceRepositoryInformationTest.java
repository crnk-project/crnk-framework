package io.crnk.core.resource;

import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.mock.models.Task;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.legacy.repository.information.DefaultResourceRepositoryInformationProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultResourceRepositoryInformationTest {

	private DefaultResourceRepositoryInformationProvider provider;

	private RepositoryInformationProviderContext context;

	@Before
	public void setup() {
		provider = new DefaultResourceRepositoryInformationProvider();

		ResourceInformation resourceInformation = Mockito.mock(ResourceInformation.class);

		ResourceInformationProvider informationBuilder = Mockito.mock(ResourceInformationProvider.class);
		Mockito.when(informationBuilder.accept(Mockito.any(Class.class))).thenReturn(true);
		Mockito.when(informationBuilder.build(Mockito.any(Class.class))).thenReturn(resourceInformation);

		context = Mockito.mock(RepositoryInformationProviderContext.class);
		Mockito.when(context.getResourceInformationBuilder()).thenReturn(informationBuilder);
	}

	@Test
	public void checkExposed() {
		ResourceRepositoryInformation exposedInformation =
				(ResourceRepositoryInformation) provider.build(new ExposedRepository(), context);

		ResourceRepositoryInformation defaultInformation =
				(ResourceRepositoryInformation) provider.build(new DefaultExposedRepository(), context);

		ResourceRepositoryInformation notExposedInformation =
				(ResourceRepositoryInformation) provider.build(new NotExposedRepository(), context);

		Assert.assertTrue(exposedInformation.isExposed());
		Assert.assertTrue(defaultInformation.isExposed());
		Assert.assertFalse(notExposedInformation.isExposed());
	}

	@JsonApiExposed
	public class ExposedRepository extends InMemoryResourceRepository<Task, Long> {

		public ExposedRepository() {
			super(Task.class);
		}
	}

	public class DefaultExposedRepository extends InMemoryResourceRepository<Task, Long> {

		public DefaultExposedRepository() {
			super(Task.class);
		}
	}

	@JsonApiExposed(false)
	public class NotExposedRepository extends InMemoryResourceRepository<Task, Long> {

		public NotExposedRepository() {
			super(Task.class);
		}
	}
}
