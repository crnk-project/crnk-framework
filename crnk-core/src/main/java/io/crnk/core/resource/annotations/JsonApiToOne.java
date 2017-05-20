package io.crnk.core.resource.annotations;

import java.lang.annotation.*;

/**
 * Indicates an association to single value which need to be handled by a separate repository.
 *
 * @deprecated It is recommended to to implement {@link JsonApiRelation}.
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiToOne {

	/**
	 * Defines whether the data associated to the relation should be visible when requesting information about a
	 * resource that contains this relation.
	 *
	 * @return <i>true</i> if lazy, <i>false</i> otherwise
	 */
	boolean lazy() default false;

	/**
	 * @return opposite attribute name in case of a bidirectional association. Used by {@link RelationshipRepositoryBase} to implement
	 * its findOneTarget and findManyTarget functions by directly searching in the related resource repository with a filter in the opposite direction.
	 * Allow to work with relations with only implementing resource repositories!
	 */
	String opposite() default "";
}
