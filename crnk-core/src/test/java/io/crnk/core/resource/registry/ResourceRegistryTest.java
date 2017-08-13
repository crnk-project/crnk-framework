package io.crnk.core.resource.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryPartEvent;
import io.crnk.core.engine.registry.ResourceRegistryPartListener;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.resource.annotations.JsonApiResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class ResourceRegistryTest {

	public static final String TEST_MODELS_URL = "https://service.local";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private ResourceRegistry resourceRegistry;

	private ModuleRegistry moduleRegistry;

	@Before
	public void resetResourceRegistry() {
		moduleRegistry = new ModuleRegistry();
		moduleRegistry.getHttpRequestContextProvider().setServiceUrlProvider(new ConstantServiceUrlProvider(TEST_MODELS_URL));
		resourceRegistry = new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry);
	}

	@Test
	public void onExistingTypeShouldReturnEntry() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
		assertThat(tasksEntry).isNotNull();
	}

	@Test
	public void addEntryShouldFireEvent() {
		ResourceRegistryPartListener listener = Mockito.mock(ResourceRegistryPartListener.class);
		resourceRegistry.addListener(listener);
		resourceRegistry.addEntry(newRegistryEntry(Project.class, "projects"));
		Mockito.verify(listener, Mockito.times(1)).onChanged(Mockito.any(ResourceRegistryPartEvent.class));
	}

	private <T> RegistryEntry newRegistryEntry(Class<T> repositoryClass, String path) {
		ResourceInformation resourceInformation =
				new ResourceInformation(moduleRegistry.getTypeParser(), Task.class, path, null, null);
		return new RegistryEntry(resourceInformation, new ResourceRepositoryInformationImpl(path, resourceInformation, RepositoryMethodAccess.ALL), null,
				null);
	}

	@Test
	public void testGetSeriveUrlProvider() {
		assertThat(resourceRegistry.getServiceUrlProvider().getUrl()).isEqualTo(TEST_MODELS_URL);
	}

	@Test
	public void testGetServiceUrl() {
		assertThat(resourceRegistry.getServiceUrlProvider().getUrl()).isEqualTo(TEST_MODELS_URL);
	}

	@Test
	public void onExistingClassShouldReturnEntry() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		RegistryEntry tasksEntry = resourceRegistry.findEntry(Task.class);
		assertThat(tasksEntry).isNotNull();
	}

	@Test
	public void onExistingTypeShouldReturnUrl() {
		RegistryEntry entry = resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		String resourceUrl = resourceRegistry.getResourceUrl(entry.getResourceInformation());
		assertThat(resourceUrl).isEqualTo(TEST_MODELS_URL + "/tasks");
	}

	@Test
	public void onNonExistingTypeShouldReturnNull() {
		RegistryEntry entry = resourceRegistry.getEntry("nonExistingType");
		assertThat(entry).isNull();
	}


	@Test
	public void checkHasEntry() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		Assert.assertTrue(resourceRegistry.hasEntry(Task.class));
		Assert.assertFalse(resourceRegistry.hasEntry(String.class));
	}


	@Test
	public void onNonExistingClassShouldThrowException() {
		expectedException.expect(RepositoryNotFoundException.class);
		resourceRegistry.findEntry(Long.class);
	}

	@Test(expected = RepositoryNotFoundException.class)
	public void onNonExistingClassShouldReturnNull() {
		resourceRegistry.findEntry(Long.class);
	}

	@Test
	public void onResourceClassReturnCorrectClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));

		// WHEN
		Class<?> clazz = resourceRegistry.findEntry(Task$Proxy.class).getResourceInformation().getResourceClass();

		// THEN
		assertThat(clazz).isNotNull();
		assertThat(clazz).hasAnnotation(JsonApiResource.class);
		assertThat(clazz).isEqualTo(Task.class);
	}

	@Test
	public void onResourceClassReturnCorrectParentInstanceClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		Task$Proxy resource = new Task$Proxy();

		// WHEN
		Class<?> clazz = resourceRegistry.findEntry(resource.getClass()).getResourceInformation().getResourceClass();

		// THEN
		assertThat(clazz).isEqualTo(Task.class);
	}

	@Test
	public void onResourceClassReturnCorrectInstanceClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		Task resource = new Task();

		// WHEN
		Class<?> clazz = resourceRegistry.findEntry(resource.getClass()).getResourceInformation().getResourceClass();

		// THEN
		assertThat(clazz).isEqualTo(Task.class);
	}

	@Test(expected = RepositoryNotFoundException.class)
	public void onResourceClassReturnNoInstanceClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));

		// WHEN
		resourceRegistry.findEntry(Object.class);
	}

	@Test
	public void onResourceGetEntryWithBackUp() {
		String taskType = Task.class.getAnnotation(JsonApiResource.class).type();
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, taskType));

		// WHEN
		RegistryEntry registryEntry = resourceRegistry.findEntry(Task.class);

		// THEN
		assertNotNull(registryEntry);
		assertNotNull(registryEntry.getResourceInformation().getResourceType(), taskType);

		// WHEN
		registryEntry = resourceRegistry.findEntry(Task.class);

		// THEN
		assertNotNull(registryEntry);
		assertNotNull(registryEntry.getResourceInformation().getResourceType(), taskType);
	}

	public static class Task$Proxy extends Task {

	}

}
