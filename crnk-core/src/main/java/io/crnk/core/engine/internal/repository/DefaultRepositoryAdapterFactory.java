package io.crnk.core.engine.internal.repository;

import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.legacy.repository.ResourceRepository;
import io.crnk.legacy.repository.annotations.JsonApiRelationshipRepository;
import io.crnk.legacy.repository.annotations.JsonApiResourceRepository;

public class DefaultRepositoryAdapterFactory implements RepositoryAdapterFactory {

	private final ModuleRegistry moduleRegistry;

	public DefaultRepositoryAdapterFactory(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
	}

	@Override
	public boolean accepts(Object repository) {
		return repository instanceof ResourceRepository || repository instanceof ResourceRepositoryV2
				|| repository instanceof RelationshipRepository || repository instanceof RelationshipRepositoryV2
				|| repository.getClass().getAnnotation(JsonApiResourceRepository.class) != null
				|| repository.getClass().getAnnotation(JsonApiRelationshipRepository.class) != null;
	}

	@Override
	public ResourceRepositoryAdapter createResourceRepositoryAdapter(ResourceRepositoryInformation information, Object repository) {
		return new ResourceRepositoryAdapterImpl(information, moduleRegistry, repository);
	}

	@Override
	public RelationshipRepositoryAdapter createRelationshipRepositoryAdapter(ResourceField field, RelationshipRepositoryInformation information, Object repository) {
		return new RelationshipRepositoryAdapterImpl(field, moduleRegistry, repository);
	}

	@Override
	public ResourceRepositoryAdapter decorate(ResourceRepositoryAdapter adapter) {
		return adapter;
	}

	@Override
	public RelationshipRepositoryAdapter decorate(RelationshipRepositoryAdapter adapter) {
		return adapter;
	}
}
