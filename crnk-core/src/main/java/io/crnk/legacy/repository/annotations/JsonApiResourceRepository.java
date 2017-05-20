package io.crnk.legacy.repository.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * Class annotated with this annotation will be treated as a document class for a
 * {@link JsonApiResourceRepository#value()} property.
 * </p>
 * <p>
 * Repository methods defined in a class annotated by this <i>@interface</i> can throw <b>only</b> instances of
 * {@link RuntimeException}.
 * </p>
 *
 * @see io.crnk.legacy.repository.ResourceRepository
 * @deprecated Make use of ResourceRepositoryV2 and related classes
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonApiResourceRepository {
	Class<?> value();
}
