package io.crnk.core.resource.annotations;

import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;

import java.lang.annotation.*;

/**
 * Defines a resource. Each class annotated with {@link JsonApiResource} must have defined {@link JsonApiResource#type()}.
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
	String type();

	/**
	 * Defines path of the resource specified by <i>type</i>. According to JSON API, the <i>type</i> can be either singular or
	 * plural.
	 *
	 * @return path of the <i>type</i> of the resource, default the type attribute value
	 * @see <a href="http://jsonapi.org/format/#document-structure-resource-types">JSON API - Resource Types</a>
	 */
	String resourcePath() default "";

	/**
	 * Defines paging behavior of the resource
	 * @return {@link PagingBehavior} definition
	 */
	Class<? extends PagingBehavior> pagingBehavior() default OffsetLimitPagingBehavior.class;
}