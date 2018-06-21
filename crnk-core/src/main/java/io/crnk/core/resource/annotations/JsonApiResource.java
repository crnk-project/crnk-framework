package io.crnk.core.resource.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.queryspec.pagingspec.VoidPagingBehavior;


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
	 * @deprecated use pagingSpec
	 */
	@Deprecated
	Class<? extends PagingBehavior> pagingBehavior() default VoidPagingBehavior.class;

	/**
	 * Defines paging behavior of the resource. By default it just makes use of the default/first paging behavior registered.
	 *
	 * @return {@link PagingBehavior} definition
	 */
	Class<? extends PagingSpec> pagingSpec() default PagingSpec.class;
}