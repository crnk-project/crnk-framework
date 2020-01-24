package io.crnk.core.resource.annotations;

/**
 * Defines the relationship inclusion look up strategy for a resource(s) relationship field.
 *
 * @see JsonApiRelation
 * @since 3.0
 */
public enum LookupIncludeBehavior {

    /**
     * Uses the default strategy. If {@link JsonApiRelation#repositoryBehavior()} fetches
     * from the opposite side, a manual relationship repository is implemented or {@link JsonApiRelationId} are in use,
     * {@link #AUTOMATICALLY_WHEN_NULL} is chosen. Else it will fall back
     * to {@link LookupIncludeBehavior#NONE} by default.
     */
    DEFAULT,
    /**
     * Defines that the resource repository is responsible to perform inclusion.
     */
    NONE,
    /**
     * Defines that inclusions are fetched from relationship repositories if the field is null.
     */
    AUTOMATICALLY_WHEN_NULL,
    /**
     * Defines that inclusions are always fetched from relationship repositories regardless of existing contents.
     */
    AUTOMATICALLY_ALWAYS
}
