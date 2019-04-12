package io.crnk.core.repository;

import io.crnk.core.queryspec.QuerySpec;

import java.util.Collection;
import java.util.Map;

/**
 * Repository responsible for operations on single-valued relations.
 *
 * @param <T> source class type
 * @param <I> T class id type
 * @param <D> target class type
 * @param <J> D class id type
 */
public interface OneRelationshipRepository<T, I, D, J>
        extends Repository, MatchedRelationshipRepository {

    RelationshipMatcher getMatcher();

    /**
     * Set a relation defined by a field. targetId legacy can be either in a form of an object or null value,
     * which means that if there's a relation, it should be removed. It is used only for To-One relationship.
     *
     * @param source    instance of a source class
     * @param targetId  id of a target repository
     * @param fieldName name of target's filed
     */
    void setRelation(T source, J targetId, String fieldName);

    /**
     * Find a relation's target identifier. It is used only for To-One relationship.
     *
     * @param sourceIds  an identifier of a source
     * @param fieldName name of target's filed
     * @param querySpec querySpec sent along with the request as parameters
     * @return an identifier of a target of a relation
     */
    Map<I, D> findOneRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec);

}
