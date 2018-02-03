package io.crnk.core.resource.annotations;

import io.crnk.core.repository.implicit.ImplicitOwnerBasedRelationshipRepository;

/**
 * Provides multiple strategies how relationships are handled. In many cases it can developpers to skip the manual development
 * of relationship repositories altogether and make use of an implicit one accessing the resource repositories on both sides
 * of the relationship.
 */
public enum RelationshipRepositoryBehavior {

	/**
	 * Will make use of {@link #IMPLICIT_FROM_OWNER} for relationships making use of {@link JsonApiRelationId} or
	 * {@link LookupIncludeBehavior#NONE} is used as long as the application does not provide a
	 * custom, manual implementation. In any other case it will fallback to {@link #CUSTOM}.
	 */
	DEFAULT,

	/**
	 * The application brings a long a custom {@link io.crnk.core.repository.ResourceRepositoryV2} implementation and
	 * is in full control.
	 */
	CUSTOM,

	/**
	 * A default implementation is used that will forward any relationship request to the owning resource repository. With
	 * <i>owning</i> is referred to the resource repository that manages the resource holding that relationship.
	 * <p></p>
	 * GET requests will fetch the owning resources and grab the relationship from there (with the appropriate inclusion
	 * parameter). For this to work, the resource repository must respect the inclusion parameter and lookup the
	 * relationshiop by itself. If
	 * the relationship makes use of {@link JsonApiRelationId}, the relationship repository can also fallback to getting only
	 * the ids and fetch the actual sources from the opposite resource repository.
	 * If neither is the case, you may consider the use of {@link #IMPLICIT_GET_OPPOSITE_MODIFY_OWNER}.
	 * <p>
	 * <p>POST, PATCH, DELETE
	 * requests will update the owning resource accordingly and invoke a save operation on the owning resource repository.
	 * <p>
	 * An implementation is provided by
	 * {@link ImplicitOwnerBasedRelationshipRepository}.
	 */
	IMPLICIT_FROM_OWNER,

	/**
	 * Works the same as {@link #IMPLICIT_FROM_OWNER} for PATCH, POST, DELETE methods. But will query the opposite resource
	 * repository for GET requests. For this to work {@link JsonApiRelation#opposite()} must be specified. For example,
	 * if there is a relationship between Task and Project with the project and tasks relationship fields. To get all tasks of
	 * a project, the task repository will be queried with a &quot;project.id=<i>projectId></i>&quot; filter parameter.
	 * Relational database are one typical example where the pattern fits nicely. In contract to {@IMPLICIT_FROM_OWNER}, only a
	 * single resource repository is involved with a slightly more complex filter parameter, giving performance benefits.
	 * <p>
	 * An implementation is provided by {@link io.crnk.core.repository.RelationshipRepositoryBase}.
	 */
	IMPLICIT_GET_OPPOSITE_MODIFY_OWNER

	// TODO implement in the future
	// IMPLICIT_FROM_OPPOSITE
}
