package io.crnk.core.resource.registry;

import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.RelationshipRepositoryNotFoundException;
import io.crnk.core.mock.models.*;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.registry.AnnotatedResourceEntry;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class RegistryEntryTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void onValidRelationshipClassShouldReturnRelationshipRepository() throws Exception {
		// GIVEN
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		RegistryEntry sut = new RegistryEntry(new ResourceRepositoryInformationImpl(null, null, null), new AnnotatedResourceEntry(new RepositoryInstanceBuilder(new SampleJsonServiceLocator(), TaskRepository.class)),
				(List) Collections.singletonList(new DirectResponseRelationshipEntry(new RepositoryInstanceBuilder(new SampleJsonServiceLocator(), TaskToProjectRepository.class))));

		// WHEN
		RelationshipRepositoryAdapter<Task, ?, ?, ?> relationshipRepository = sut.getRelationshipRepositoryForClass(Project.class, null);

		// THEN
		assertThat(relationshipRepository).isExactlyInstanceOf(RelationshipRepositoryAdapter.class);
	}

	@Test
	public void onInvalidRelationshipClassShouldThrowException() throws Exception {
		// GIVEN
		ResourceRepositoryInformation resourceInformation = newRepositoryInformation(Task.class, "tasks");
		RegistryEntry sut = new RegistryEntry(resourceInformation, null,
				(List) Collections.singletonList(new DirectResponseRelationshipEntry(new RepositoryInstanceBuilder(new SampleJsonServiceLocator(), TaskToProjectRepository.class))));

		// THEN
		expectedException.expect(RelationshipRepositoryNotFoundException.class);

		// WHEN
		sut.getRelationshipRepositoryForClass(User.class, null);
	}

	private <T> ResourceRepositoryInformation newRepositoryInformation(Class<T> repositoryClass, String path) {
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		TypeParser typeParser = moduleRegistry.getTypeParser();
		return new ResourceRepositoryInformationImpl(null, path, new ResourceInformation(typeParser, Task.class, path, null, null));
	}

	@Test
	public void onValidParentShouldReturnTrue() throws Exception {
		// GIVEN
		RegistryEntry thing = new RegistryEntry(newRepositoryInformation(Thing.class, "things"), null);
		RegistryEntry document = new RegistryEntry(newRepositoryInformation(Document.class, "documents"), null);
		document.setParentRegistryEntry(thing);
		RegistryEntry memorandum = new RegistryEntry(newRepositoryInformation(Memorandum.class, "memorandum"), null);
		memorandum.setParentRegistryEntry(document);

		// WHEN
		boolean result = memorandum.isParent(thing);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onInvalidParentShouldReturnFalse() throws Exception {
		// GIVEN
		RegistryEntry document = new RegistryEntry(newRepositoryInformation(Document.class, "documents"), null);
		RegistryEntry task = new RegistryEntry(newRepositoryInformation(Task.class, "tasks"), null);

		// WHEN
		boolean result = document.isParent(task);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void equalsContract() throws NoSuchFieldException {
		ModuleRegistry moduleRegistry = new ModuleRegistry();
		RegistryEntry blue = new RegistryEntry(newRepositoryInformation(String.class, "strings"), null);
		RegistryEntry red = new RegistryEntry(newRepositoryInformation(Long.class, "longs"), null);
		TypeParser typeParser = moduleRegistry.getTypeParser();
		EqualsVerifier.forClass(RegistryEntry.class).withPrefabValues(RegistryEntry.class, blue, red)
				.withPrefabValues(ResourceInformation.class, new ResourceInformation(typeParser, String.class, null, null, null), new ResourceInformation(typeParser, Long.class, null, null, null, null))
				.withPrefabValues(Field.class, String.class.getDeclaredField("value"), String.class.getDeclaredField("hash")).withPrefabValues(ModuleRegistry.class, new ModuleRegistry(), new ModuleRegistry())
				.suppress(Warning.NONFINAL_FIELDS).suppress(Warning.STRICT_INHERITANCE).verify();
	}
}
