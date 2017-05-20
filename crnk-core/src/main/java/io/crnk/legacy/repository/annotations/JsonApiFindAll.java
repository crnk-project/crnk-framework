package io.crnk.legacy.repository.annotations;

import io.crnk.legacy.queryParams.QueryParams;

import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to perform find all
 * operation. The method must be defined in a class annotated with
 * {@link JsonApiResourceRepository}.
 * </p>
 * <p>
 * There are no requirements on the method parameters.
 * </p>
 * <p>
 * The return value must be an {@link Iterable} of resources of
 * {@link JsonApiResourceRepository#value()} type.
 * </p>
 *
 * @see io.crnk.legacy.repository.ResourceRepository#findAll(QueryParams)
 * @deprecated Make use of ResourceRepositoryV2 and related classes
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiFindAll {
}
