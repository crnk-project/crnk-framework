package io.crnk.core.resource.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Decides whether a given repository is exposed on the JSON API endpoint. Enabled by default, but may not always desirable, for
 * example:
 *
 * <ul>
 * <li>A micro-service having registered a remote repository of another micro-service. Such repositories are useful to perform
 * relationship lookups, but they likely must not be exposed again on the second micro-service.</li>
 * <li>
 * Nested resources that have a parent resource may or may not be queryable on their own.
 * </li>
 * </ul>
 *
 * @since 2.8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JsonApiExposed {

	/**
	 * @return true if the attribute can be sorted.
	 */
	boolean value() default true;
}
