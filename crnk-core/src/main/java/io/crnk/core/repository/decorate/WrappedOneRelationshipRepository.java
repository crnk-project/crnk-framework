package io.crnk.core.repository.decorate;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.RelationshipMatcher;

import java.util.Collection;
import java.util.Map;

public class WrappedOneRelationshipRepository<T, I, D, J>
        implements OneRelationshipRepository<T, I, D, J>, Wrapper {

    protected OneRelationshipRepository<T, I, D, J> wrappedRepository;

    public WrappedOneRelationshipRepository(OneRelationshipRepository<T, I, D, J> wrappedRepository) {
        this.wrappedRepository = wrappedRepository;
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
    public Map<I, D> findOneRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
        return wrappedRepository.findOneRelations(sourceIds, fieldName, querySpec);
    }

    @Override
    public Object getWrappedObject() {
        return wrappedRepository;
    }
}
