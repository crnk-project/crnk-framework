package io.crnk.core.resource.annotations;

import java.lang.annotation.*;

import io.crnk.core.boot.CrnkProperties;

import static io.crnk.core.resource.annotations.LookupIncludeBehavior.DEFAULT;
import static io.crnk.core.resource.annotations.SerializeType.LAZY;

/**
 * Indicates an association to either a single value or collection which needs to be handled by a separate
 * relationship resource.
 *
 * @since 3.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiRelation {


	/**
	 * Name of the idField annotated with @JsonApiRelationId.
	 */
	String idField() default "";

	/**
	 * (Optional) Defines whether the data associated to the relationship should be serialized when making a request.
	 * <p>
	 * LAZY (Default) - is serialize the relationship when requested with an include query legacy.
	 * <p>
	 * ONLY_ID - is only serialize the ids in the resources relationship section but not the included section.
	 * <p>
	 * EAGER - is always serialize this relationship.
	 */
	SerializeType serialize() default LAZY;

	/**
	 * (Optional) This attribute is used to make automatic value assignment using a defined relationship resource if such resource
	 * is available.
	 * <p>
	 * DEFAULT (Default) - consults the global lookup behavior set by {@link CrnkProperties#INCLUDE_AUTOMATICALLY} and
	 * {@link CrnkProperties#INCLUDE_AUTOMATICALLY_OVERWRITE} first, using the value set by these global settings.  If not
	 * set globally, this setting will fall back to {@link LookupIncludeBehavior#NONE}.
	 * <p>
	 * NONE - do not automatically call this fields relationship findManyTargets or findOneTarget.
	 * <p>
	 * AUTOMATICALLY_WHEN_NULL - automatically perform a relationship findManyTargets or findOneTarget when this field's value
	 * is null and it is either A. requested in an include query legacy B. SerializeType.ONLY_ID or SerializeType.EAGER
	 * is present.
	 * <p>
	 * AUTOMATICALLY_ALWAYS - always automatically call a relationship's findManyTargets or findOneTarget and overwrite this field.
	 */
	LookupIncludeBehavior lookUp() default DEFAULT;

	/**
	 * @return opposite attribute name in case of a bidirectional association. Used by {@link io.crnk.core.repository.RelationshipRepositoryBase} to implement
	 * its findOneTarget and findManyTarget functions by directly searching in the related resource repository with a filter in the opposite direction.
	 * Allow to work with relations with only implementing resource repositories!
	 */
	String opposite() default "";
}
