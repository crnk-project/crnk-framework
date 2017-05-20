package io.crnk.legacy.repository;

import io.crnk.core.repository.Repository;
import io.crnk.legacy.queryParams.QueryParams;

import java.io.Serializable;

/**
 * <p>
 * Base unidirectional document responsible for operations on relations. All of the methods in this interface have
 * fieldName field as last parameter. It solves a problem of many relationships between the same resources.
 * <p>
 * There are two methods that are used for To-One relationships:
 * <ul>
 * <li>setRelation</li>
 * <li>findOneTarget</li>
 * </ul>
 * <p>
 * There are four methods that are used for To-Many relationships:
 * <ul>
 * <li>setRelations</li>
 * <li>addRelation</li>
 * <li>removeRelation</li>
 * <li>findManyTargets</li>
 * </ul>
 * <p>
 * The reason why there is more than one method for To-Many relationships manipulation is to prevent
 * <a href="https://en.wikipedia.org/wiki/Race_condition">race condition</a> situations in which a field could be
 * changed concurrently by another request.
 *
 * @param <T>    source class type
 * @param <T_ID> T class id type
 * @param <D>    target class type
 * @param <D_ID> D class id type
 * @deprecated Make use of RelationshipRepositoryV2
 */
@Deprecated
public interface RelationshipRepository<T, T_ID extends Serializable, D, D_ID extends Serializable> extends Repository {

	int TARGET_TYPE_GENERIC_PARAMETER_IDX = 2;

	/**
	 * Set a relation defined by a field. targetId parameter can be either in a form of an object or null value,
	 * which means that if there's a relation, it should be removed. It is used only for To-One relationship.
	 *
	 * @param source    instance of a source class
	 * @param targetId  id of a target document
	 * @param fieldName name of target's filed
	 */
	void setRelation(T source, D_ID targetId, String fieldName);

	/**
	 * Set a relation defined by a field. TargetIds parameter can be either in a form of an object or null value,
	 * which means that if there's a relation, it should be removed. It is used only for To-Many relationship.
	 *
	 * @param source    instance of a source class
	 * @param targetIds ids of a target document
	 * @param fieldName name of target's filed
	 */
	void setRelations(T source, Iterable<D_ID> targetIds, String fieldName);

	/**
	 * Add a relation to a field. It is used only for To-Many relationship, that is if this method is called, a new
	 * relationship should be added to the set of the relationships.
	 *
	 * @param source    instance of source class
	 * @param targetIds ids of the target document
	 * @param fieldName name of target's field
	 */
	void addRelations(T source, Iterable<D_ID> targetIds, String fieldName);

	/**
	 * Removes a relationship from a set of relationships. It is used only for To-Many relationship.
	 *
	 * @param source    instance of source class
	 * @param targetIds ids of the target document
	 * @param fieldName name of target's field
	 */
	void removeRelations(T source, Iterable<D_ID> targetIds, String fieldName);

	/**
	 * Find a relation's target identifier. It is used only for To-One relationship.
	 *
	 * @param sourceId    an identifier of a source
	 * @param fieldName   name of target's filed
	 * @param queryParams parameters sent along with the request
	 * @return an identifier of a target of a relation
	 */
	D findOneTarget(T_ID sourceId, String fieldName, QueryParams queryParams);

	/**
	 * Find a relation's target identifiers. It is used only for To-Many relationship.
	 *
	 * @param sourceId    an identifier of a source
	 * @param fieldName   name of target's filed
	 * @param queryParams parameters sent along with the request
	 * @return identifiers of targets of a relation
	 */
	Iterable<D> findManyTargets(T_ID sourceId, String fieldName, QueryParams queryParams);
}
