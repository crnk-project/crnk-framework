package io.crnk.core.resource.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Defines a structured type with on or more fields that can be used
 * as attribute of a resource. In spirit it is the same as a JPA @Embeddable.
 * Structured types can then also be sorted, filtered, requested with field
 * sets and patched.
 * <p>
 * Embeddables can host any kind of primitive field, collection or map. However,
 * they do not allow the introduction of relationships (incompatible to JSON:API and
 * the general semantic of restful services with HATEOAS).
 *
 * @since 2.12 to distinguish primitive (dates, strings, etc.) from structured types.
 * Later releases will make this annotation mandatory. Currently there is
 * only a heuristic in place of what is structured and what not.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonApiEmbeddable {

}