package io.crnk.core.resource.annotations;

import java.lang.annotation.*;

/**
 * Allows to configure the supported features of a field.
 *
 * @since 3.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiField {

	/**
	 * @return true if the attribute can be sorted.
	 */
	boolean sortable() default true;

	/**
	 * @return true if the attribute can be filtered.
	 */
	boolean filterable() default true;

	/**
	 * @return true if the attribute can be set with a POST request.
	 */
	boolean postable() default true;

	/**
	 * @return true if the attribute can be changed with a PATCH request.
	 */
	boolean patchable() default true;

}
