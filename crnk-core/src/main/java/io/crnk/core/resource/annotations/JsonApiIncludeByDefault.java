package io.crnk.core.resource.annotations;

import io.crnk.core.exception.ResourceException;

import java.lang.annotation.*;

/**
 * Indicates additional resources that should be included by default with every primary resource.
 * The field can be added to every relation defined by {@link JsonApiToOne} or {@link JsonApiToMany}. Otherwise,
 * {@link ResourceException} will be thrown at the initialization phrase.
 *
 * @deprecated It is recommended to to implement {@link JsonApiRelation}.
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiIncludeByDefault {
}
