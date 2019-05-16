package io.crnk.core.repository.decorate;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;

/**
 * @deprecated in favor of {@link WrappedOneRelationshipRepository} and  {@link WrappedManyRelationshipRepository}
 */
@Deprecated
public class WrappedRelationshipRepository<T, I, D, J>
        implements RelationshipRepository<T, I, D, J>, Wrapper {

    protected RelationshipRepository<T, I, D, J> wrappedRepository;

    public WrappedRelationshipRepository(RelationshipRepository<T, I, D, J> wrappedRepository) {
        this.wrappedRepository = wrappedRepository;
    }

    @Override
    public Class<T> getSourceResourceClass() {
        return wrappedRepository.getSourceResourceClass();
    }

    @Override
    public Class<D> getTargetResourceClass() {
        return wrappedRepository.getTargetResourceClass();
    }

    @Override
    public RelationshipMatcher getMatcher() {
        return wrappedRepository.getMatcher();
    }

    @Override
    public void setRelation(T source, J targetId, String fieldName) {
        wrappedRepository.setRelation(source, targetId, fieldName);
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
    public D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
        return wrappedRepository.findOneTarget(sourceId, fieldName, querySpec);
    }

    @Override
    public ResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
        return wrappedRepository.findManyTargets(sourceId, fieldName, querySpec);
    }

    @Override
    public Object getWrappedObject() {
        return wrappedRepository;
    }
}
