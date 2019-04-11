package io.crnk.core.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

import java.io.Serializable;

/**
 * <p>
 * Base unidirectional repository responsible for operations on relations. All of the methods in this interface have
 * fieldName field as last legacy. It solves a problem of many relationships between the same resources.
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
 * @param <T> source class type
 * @param <I> T class id type
 * @param <D> target class type
 * @param <J> D class id type
 */
public interface RelationshipRepository<T, I extends Serializable, D, J extends Serializable>
		extends Repository, MatchedRelationshipRepository {

	/**
	 * @return the class that specifies the relation. Can be null if getMatcher() is implemented.
	 */
	Class<T> getSourceResourceClass();

	/**
	 * @return the related resource class returned by this repository. Can be null if getMatcher() is implemented.
	 */
	Class<D> getTargetResourceClass();


	@Override
	default RelationshipMatcher getMatcher() {
		if (this instanceof UntypedRelationshipRepository) {
			UntypedRelationshipRepository untyped = (UntypedRelationshipRepository) this;
			RelationshipMatcher matcher = new RelationshipMatcher();
			matcher.rule().source(untyped.getSourceResourceType()).target(untyped.getTargetResourceType()).add();
			return matcher;
		}

		Class<T> sourceResourceClass = getSourceResourceClass();
		Class<D> targetResourceClass = getTargetResourceClass();
		if (sourceResourceClass != null && targetResourceClass != null) {
			RelationshipMatcher matcher = new RelationshipMatcher();
			matcher.rule().source(getSourceResourceClass()).target(targetResourceClass).add();
			return matcher;
		}
		throw new IllegalStateException("implement getMatcher()");
	}

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
	 * Set a relation defined by a field. TargetIds legacy can be either in a form of an object or null value,
	 * which means that if there's a relation, it should be removed. It is used only for To-Many relationship.
	 *
	 * @param source    instance of a source class
	 * @param targetIds ids of a target repository
	 * @param fieldName name of target's filed
	 */
	void setRelations(T source, Iterable<J> targetIds, String fieldName);

	/**
	 * Add a relation to a field. It is used only for To-Many relationship, that is if this method is called, a new
	 * relationship should be added to the set of the relationships.
	 *
	 * @param source    instance of source class
	 * @param targetIds ids of the target resource
	 * @param fieldName name of target's field
	 */
	void addRelations(T source, Iterable<J> targetIds, String fieldName);

	/**
	 * Removes a relationship from a set of relationships. It is used only for To-Many relationship.
	 *
	 * @param source    instance of source class
	 * @param targetIds ids of the target repository
	 * @param fieldName name of target's field
	 */
	void removeRelations(T source, Iterable<J> targetIds, String fieldName);

	/**
	 * Find a relation's target identifier. It is used only for To-One relationship.
	 *
	 * @param sourceId  an identifier of a source
	 * @param fieldName name of target's filed
	 * @param querySpec querySpec sent along with the request as parameters
	 * @return an identifier of a target of a relation
	 */
	D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec);

	/**
	 * Find a relation's target identifiers. It is used only for To-Many relationship.
	 *
	 * @param sourceId  an identifier of a source
	 * @param fieldName name of target's filed
	 * @param querySpec querySpec sent along with the request as parameters
	 * @return identifiers of targets of a relation
	 */
	ResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec);

}
