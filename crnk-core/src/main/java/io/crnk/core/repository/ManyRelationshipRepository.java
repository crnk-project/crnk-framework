package io.crnk.core.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;
import java.util.Map;

/**
 * Repository responsible for operations on multi-valued relations.
 *
 * @param <T> source class type
 * @param <I> T class id type
 * @param <D> target class type
 * @param <J> D class id type
 */
public interface ManyRelationshipRepository<T, I, D, J>
        extends Repository, MatchedRelationshipRepository {

    RelationshipMatcher getMatcher();

    /**
     * Set a relation defined by a field. TargetIds legacy can be either in a form of an object or null value,
     * which means that if there's a relation, it should be removed. It is used only for To-Many relationship.
     *
     * @param source    instance of a source class
     * @param targetIds ids of a target repository
     * @param fieldName name of target's filed
     */
    void setRelations(T source, Collection<J> targetIds, String fieldName);

    /**
     * Add a relation to a field. It is used only for To-Many relationship, that is if this method is called, a new
     * relationship should be added to the set of the relationships.
     *
     * @param source    instance of source class
     * @param targetIds ids of the target resource
     * @param fieldName name of target's field
     */
    void addRelations(T source, Collection<J> targetIds, String fieldName);

    /**
     * Removes a relationship from a set of relationships. It is used only for To-Many relationship.
     *
     * @param source    instance of source class
     * @param targetIds ids of the target repository
     * @param fieldName name of target's field
     */
    void removeRelations(T source, Collection<J> targetIds, String fieldName);

    /**
     * Find a relation's target identifiers. It is used only for To-Many relationship.
     *
     * @param sourceIds an identifier of a source
     * @param fieldName name of target's filed
     * @param querySpec querySpec sent along with the request as parameters
     * @return identifiers of targets of a relation
     */
    Map<I, ResourceList<D>> findManyRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec);

}
