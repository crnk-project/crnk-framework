package io.crnk.core.repository.decorate;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;
import java.util.Map;

public class WrappedManyRelationshipRepository<T, I, D, J>
        implements ManyRelationshipRepository<T, I, D, J>, Wrapper {

    protected ManyRelationshipRepository<T, I, D, J> wrappedRepository;

    public WrappedManyRelationshipRepository(ManyRelationshipRepository<T, I, D, J> wrappedRepository) {
        this.wrappedRepository = wrappedRepository;
    }

    @Override
    public RelationshipMatcher getMatcher() {
        return wrappedRepository.getMatcher();
    }

    @Override
    public void setRelations(T source, Collection<J> targetIds, String fieldName) {
        wrappedRepository.setRelations(source, targetIds, fieldName);
    }

    @Override
    public void addRelations(T source, Collection<J> targetIds, String fieldName) {
        wrappedRepository.addRelations(source, targetIds, fieldName);
    }

    @Override
    public void removeRelations(T source, Collection<J> targetIds, String fieldName) {
        wrappedRepository.removeRelations(source, targetIds, fieldName);
    }

    @Override
    public Map<I, ResourceList<D>> findManyRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
        return wrappedRepository.findManyRelations(sourceIds, fieldName, querySpec);
    }

    @Override
    public Object getWrappedObject() {
        return wrappedRepository;
    }
}
