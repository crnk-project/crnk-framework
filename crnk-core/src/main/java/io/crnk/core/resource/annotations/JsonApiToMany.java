package io.crnk.core.resource.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates an association to many values which need to be handled by a separate repository.
 *
 * @deprecated It is recommended to to implement {@link JsonApiRelation}.
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiToMany {

	/**
	 * Defines whether the data associated to the relation should be visible when requesting information about a
	 * resource that contains this relation.
	 *
	 * @return <i>true</i> if lazy, <i>false</i> otherwise
	 */
	boolean lazy() default true;

	/**
	 * @return opposite attribute name in case of a bidirectional association. Used by {@link RelationshipRepositoryBase} to implement
	 * its findOneRelations and findManyTarget functions by directly searching in the related resource repository with a filter in the opposite direction.
	 * Allow to work with relations with only implementing resource repositories!
	 */
	String opposite() default "";
}
