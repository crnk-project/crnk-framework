package io.crnk.core.resource.annotations;

import io.crnk.legacy.queryParams.QueryParams;

import java.io.Serializable;
import java.lang.annotation.*;

/**
 * This annotation is used to make automatic value assignment using a defined relationship repository if such repository
 * is available. It can be used to leave resource relationships from a resource repository not populated and make
 * Crnk call either {@link io.crnk.legacy.repository.RelationshipRepository#findOneTarget(Serializable, String, QueryParams)}
 * or {@link io.crnk.legacy.repository.RelationshipRepository#findManyTargets(Serializable, String, QueryParams)}
 * depending on the multiplicity of the relationship.
 *
 * @deprecated It is recommended to to implement {@link JsonApiRelation}.
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiLookupIncludeAutomatically {

	/**
	 * Defines whether Crnk should overwrite the value of the related object on the resource when setting inclusions
	 *
	 * @return true if the related object field is to be overwritten, false otherwise
	 */
	boolean overwrite() default false;
}
