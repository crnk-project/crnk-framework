package io.crnk.core.resource.registry;

import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.mock.models.Document;
import io.crnk.core.mock.models.Memorandum;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.Thing;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class RegistryEntryTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void onInvalidRelationshipClassShouldThrowException() throws Exception {
		// GIVEN
		ResourceRepositoryInformation repositoryInformation = newRepositoryInformation(Task.class, "tasks");
		ResourceField relationshipField = repositoryInformation.getResourceInformation().get().findFieldByUnderlyingName
				("tasks");
		Map relRepos = new HashMap<>();
		relRepos.put(relationshipField, new DirectResponseRelationshipEntry(
				new RepositoryInstanceBuilder(new SampleJsonServiceLocator(), TaskToProjectRepository.class)));
		RegistryEntry sut =
				new RegistryEntry(new DirectResponseResourceEntry(null, repositoryInformation), relRepos);

		// THEN
		expectedException.expect(ResourceFieldNotFoundException.class);

		// WHEN
		sut.getRelationshipRepository("users", null);
	}

	private ResourceEntry newResourceEntry(Class repositoryClass, String path) {
		return new DirectResponseResourceEntry(null, newRepositoryInformation(repositoryClass, path));
	}

	private <T> ResourceRepositoryInformation newRepositoryInformation(Class<T> repositoryClass, String path) {
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		TypeParser typeParser = moduleRegistry.getTypeParser();
		return new ResourceRepositoryInformationImpl(path, new ResourceInformation(typeParser, Task.class, path,null, null, null),
				RepositoryMethodAccess.ALL);
	}

	@Test
	public void onValidParentShouldReturnTrue() throws Exception {
		// GIVEN
		RegistryEntry thing = new RegistryEntry(newResourceEntry(Thing.class, "things"));
		RegistryEntry document = new RegistryEntry(newResourceEntry(Document.class, "documents"));
		document.setParentRegistryEntry(thing);
		RegistryEntry memorandum = new RegistryEntry(newResourceEntry(Memorandum.class, "memorandum"));
		memorandum.setParentRegistryEntry(document);

		// WHEN
		boolean result = memorandum.isParent(thing);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onInvalidParentShouldReturnFalse() throws Exception {
		// GIVEN
		RegistryEntry document = new RegistryEntry(newResourceEntry(Document.class, "documents"));
		RegistryEntry task = new RegistryEntry(newResourceEntry(Task.class, "tasks"));

		// WHEN
		boolean result = document.isParent(task);

		// THEN
		assertThat(result).isFalse();
	}
}
