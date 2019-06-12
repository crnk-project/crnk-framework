package io.crnk.core.repository;

import io.crnk.core.exception.MethodNotAllowedException;

/**
 * Read-only implementation
 */
public abstract class OneRelationshipRepositoryBase<T, I, D, J> implements OneRelationshipRepository<T, I, D, J> {

    @Override
    public void setRelation(T source, J targetId, String fieldName) {
        throw new MethodNotAllowedException("cannoted be updated");
    }
}
