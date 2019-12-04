package io.crnk.core.engine.internal.repository;

import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.ResourceRepository;

import java.util.Objects;

public class DefaultRepositoryAdapterFactory implements RepositoryAdapterFactory {

    private final ModuleRegistry moduleRegistry;

    public DefaultRepositoryAdapterFactory(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    @Override
    public boolean accepts(Object repository) {
        Objects.requireNonNull(repository);
        return repository instanceof ResourceRepository || repository instanceof ManyRelationshipRepository
                || repository instanceof OneRelationshipRepository;
    }

    @Override
    public ResourceRepositoryAdapter createResourceRepositoryAdapter(ResourceRepositoryInformation information, Object repository) {
        return new ResourceRepositoryAdapterImpl(information, moduleRegistry, (ResourceRepository) repository);
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
