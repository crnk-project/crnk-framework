package io.crnk.core.resource.annotations;

import java.lang.annotation.*;

/**
 * Defines a resource. Each class annotated with {@link JsonApiResource} must have defined {@link JsonApiResource#value()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonApiResource {

	/**
	 * Defines name of the resource called <i>type</i>. According to JSON API, the <i>type</i> can be either singular or
	 * plural.
	 *
	 * @return <i>type</i> of the resource
	 * @see <a href="http://jsonapi.org/format/#document-structure-resource-types">JSON API - Resource Types</a>
	 */
	String value();
}
