package io.crnk.core.engine.internal.registry;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.DefaultInformationBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.RegistryEntryBuilder;
import io.crnk.core.engine.registry.ResourceEntry;
import io.crnk.core.engine.registry.ResponseRelationshipEntry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.legacy.internal.DirectResponseRelationshipEntry;
import io.crnk.legacy.internal.DirectResponseResourceEntry;
import io.crnk.legacy.registry.RepositoryInstanceBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultRegistryEntryBuilder implements RegistryEntryBuilder {

	private final DefaultInformationBuilder informationBuilder;
	private ModuleRegistry moduleRegistry;

	private DefaultResourceRepository resourceRepository;

	private Map<String, DefaultRelationshipRepository> relationshipRepositoryMap = new HashMap<>();

	private InformationBuilder.Resource resource;

	public DefaultRegistryEntryBuilder(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
		this.informationBuilder = moduleRegistry.getInformationBuilder();
	}


	class DefaultResourceRepository implements ResourceRepository {

		private Object instance;

		private InformationBuilder.ResourceRepository information;

		public DefaultResourceRepository() {
			this.information = informationBuilder.createResourceRepository();
		}

		@Override
		public InformationBuilder.ResourceRepository information() {
			return information;
		}

		@Override
		public void instance(Object instance) {
			this.instance = instance;
		}
	}

	class DefaultRelationshipRepository implements RelationshipRepository {

		private final String targetResourceType;

		private InformationBuilder.RelationshipRepository information;

		private Object instance;


		public DefaultRelationshipRepository(String targetResourceType) {
			this.targetResourceType = targetResourceType;
			this.information = informationBuilder.createRelationshipRepository(targetResourceType);
		}

		@Override
		public InformationBuilder.RelationshipRepository information() {
			return information;
		}

		@Override
		public void instance(Object instance) {
			this.instance = instance;
		}
	}

	@Override
	public ResourceRepository resourceRepository() {
		if (resourceRepository == null) {
			resourceRepository = new DefaultResourceRepository();
		}
		return resourceRepository;
	}

	@Override
	public InformationBuilder.Resource resource() {
		if (resource == null) {
			resource = informationBuilder.createResource(null, null);
		}
		return resource;
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
		ResourceInformation resourceInformation = resource.build();

		resourceRepository.information().setResourceInformation(resourceInformation);

		ResourceRepositoryInformation repositoryInformation = resourceRepository.information().build();

		final Object decoratedResourceRepository = moduleRegistry.decorateRepository(resourceRepository.instance);
		RepositoryInstanceBuilder repositoryInstanceBuilder = new RepositoryInstanceBuilder(null, resourceRepository.instance.getClass()) {

			@Override
			public Object buildRepository() {
				return decoratedResourceRepository;
			}
		};

		ResourceEntry resourceEntry = new DirectResponseResourceEntry(repositoryInstanceBuilder);
		List<ResponseRelationshipEntry> relationshipEntries = new ArrayList<>();
		for (final DefaultRelationshipRepository relationshipRepository : relationshipRepositoryMap.values()) {
			final Object decoratedRelationshipRepository = moduleRegistry.decorateRepository(relationshipRepository.instance);
			Class repositoryClass = relationshipRepository.information.getClass();
			RepositoryInstanceBuilder<Object> relationshipInstanceBuilder = new RepositoryInstanceBuilder<Object>(null, repositoryClass) {

				@Override
				public Object buildRepository() {
					return decoratedRelationshipRepository;
				}
			};

			ResponseRelationshipEntry relationshipEntry = new DirectResponseRelationshipEntry(relationshipInstanceBuilder) {

				@Override
				public String getTargetResourceType() {
					return relationshipRepository.targetResourceType;
				}
			};
			relationshipEntries.add(relationshipEntry);
		}

		RegistryEntry entry = new RegistryEntry(resourceInformation, repositoryInformation, resourceEntry, relationshipEntries);
		entry.initialize(moduleRegistry);
		return entry;
	}
}
