package io.crnk.jpa.annotations;

import io.crnk.core.resource.annotations.JsonApiResource;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Allows to specify a resource type for an exposed entity. This annotation is
 * optional. By default the resource type is derived from the entity name (e.g.
 * Person => person).
 * <p>
 * The annotation corresponds to the default {@link JsonApiResource} annotation,
 * but with the additional benefit of reading JPA annotations to detect primary
 * keys, relationships, etc. without having to define redudant Crnk
 * annotations.
 *
 * Use {@link JsonApiResource} instead of this annotation.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Deprecated
public @interface JpaResource {

	/**
	 * Defines the type of the resource.
	 */
	String type();

	/**
	 * Defines resource path for JPA entity.
	 */
	String resourcePath() default "";
}
