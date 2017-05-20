package io.crnk.legacy.repository.annotations;

import io.crnk.legacy.queryParams.QueryParams;

import java.io.Serializable;
import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to perform find one
 * operation on a particular document. The method must be defined in a class
 * annotated with {@link JsonApiResourceRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 * <li>A document identifier</li>
 * </ol>
 * <p>
 * The return value should be a document of
 * {@link JsonApiResourceRepository#value()} type.
 * </p>
 *
 * @see io.crnk.legacy.repository.ResourceRepository#findOne(Serializable,
 * QueryParams)
 * @deprecated Make use of ResourceRepositoryV2 and related classes
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiFindOne {
}
