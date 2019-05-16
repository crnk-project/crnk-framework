package io.crnk.core.resource.annotations;

import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.foward.ForwardingRelationshipRepository;

/**
 * Provides multiple strategies how relationships are handled. In many cases it can developpers to skip the manual development
 * of relationship repositories altogether and make use of an implicit one accessing the resource repositories on both sides
 * of the relationship.
 */
public enum RelationshipRepositoryBehavior {

    /**
     * Will make use of {@link #CUSTOM} if a custom relationship repository implementation is provided.
     * {@link JsonApiRelation#mappedBy()} triggers the use of {@link #FORWARD_OPPOSITE}. If
     * {@link JsonApiRelation#lookUp()} is set to NONE it will make use of {@link #FORWARD_OWNER}.
     * If none of conditions match, an exception will be thrown as no relationship repository can back the
     * relationship.
     */
    DEFAULT,

    /**
     * The application brings a long a custom {@link ResourceRepository} implementation and
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
     * If neither is the case, you may consider the use of {@link #FORWARD_GET_OPPOSITE_SET_OWNER}.
     * <p>
     * <p>POST, PATCH, DELETE
     * requests will update the owning resource accordingly and invoke a save operation on the owning resource repository.
     * <p>
     * An implementation is provided by
     * {@link io.crnk.core.repository.foward.ForwardingRelationshipRepository}.
     */
    FORWARD_OWNER,

    /**
     * Works the same as {@link #FORWARD_OWNER} for PATCH, POST, DELETE methods. But will query the opposite resource
     * repository for GET requests. For this to work {@link JsonApiRelation#mappedBy()} ()} must be specified. For example,
     * if there is a relationship between Task and Project with the project and tasks relationship fields. To get all tasks of
     * a project, the task repository will be queried with a &quot;project.id=<i>projectId></i>&quot; filter parameter.
     * Relational database are one typical example where the pattern fits nicely. In contrast to  {@link #FORWARD_OWNER}, only a
     * single resource repository is involved with a slightly more complex filter parameter, giving performance benefits.
     * <p>
     * An implementation is provided by {@link ForwardingRelationshipRepository}.
     */
    FORWARD_GET_OPPOSITE_SET_OWNER,

    /**
     * Opposite to {@link #FORWARD_OWNER}.
     * <p>
     * An implementation is provided by {@link ForwardingRelationshipRepository}.
     */
    FORWARD_OPPOSITE,

}
