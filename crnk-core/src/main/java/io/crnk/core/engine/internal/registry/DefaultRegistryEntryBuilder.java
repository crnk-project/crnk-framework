package io.crnk.core.engine.internal.registry;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.RegistryEntryBuilder;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultRegistryEntryBuilder implements RegistryEntryBuilder {

	private ModuleRegistry moduleRegistry;

	private DefaultResourceRepository resourceRepository = new DefaultResourceRepository();

	private Map<String, DefaultRelationshipRepository> relationshipRepositoryMap = new HashMap<>();

	public DefaultRegistryEntryBuilder(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
	}


	class DefaultResourceRepository implements ResourceRepository {

		private Object instance;

		@Override
		public InformationBuilder.ResourceRepository information() {
			return null;
		}

		@Override
		public void instance(Object instance) {
			this.instance = instance;
		}
	}

	class DefaultRelationshipRepository implements RelationshipRepository {

		private final String targetResourceType;

		private Object instance;

		public DefaultRelationshipRepository(String targetResourceType) {
			this.targetResourceType = targetResourceType;

			//this.information = new DefaultInformationBuilder().createRelationshipRepository();

		}

		@Override
		public InformationBuilder.ResourceRepository information() {
			return null;
		}

		@Override
		public void instance(Object instance) {
			this.instance = instance;
		}
	}

	@Override
	public ResourceRepository resourceRepository() {
		return resourceRepository;
	}

	@Override
	public RelationshipRepository relationshipRepository(String targetResourceType) {
		DefaultRelationshipRepository repository = relationshipRepositoryMap.get(targetResourceType);
		if (repository == null) {
			repository = new DefaultRelationshipRepository(targetResourceType);
			relationshipRepositoryMap.put(targetResourceType, repository);
		}
		return repository;
	}

	@Override
	public RegistryEntry build() {


		ResourceRepositoryInformation repositoryInformation = resourceRepository.information().build();

// FIXME
		//	final Object decoratedRepository = decorateRepository(repository);

		RepositoryInstanceBuilder repositoryInstanceBuilder = new RepositoryInstanceBuilder(null, resourceRepository.instance.getClass()) {

			@Override
			public Object buildRepository() {
				return resourceRepository.instance;
			}
		};

		ResourceEntry resourceEntry = new DirectResponseResourceEntry(repositoryInstanceBuilder);
		List<ResponseRelationshipEntry> relationshipEntries = new ArrayList<>();
		for (DefaultRelationshipRepository relationshipRepository : relationshipRepositoryMap.values()) {

		}


		return new RegistryEntry();
	}
}
