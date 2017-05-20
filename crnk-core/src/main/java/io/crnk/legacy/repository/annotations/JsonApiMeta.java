package io.crnk.legacy.repository.annotations;

import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to provide meta information for a set of resources. The method
 * must be defined in a class annotated with {@link JsonApiResourceRepository} or
 * {@link JsonApiRelationshipRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 * <li>A list of resources</li>
 * </ol>
 * <p>
 * The return value must be an instance of {@link io.crnk.core.resource.meta.MetaInformation} type.
 * </p>
 *
 * @see io.crnk.legacy.repository.MetaRepository
 * @deprecated Make use of ResourceRepositoryV2 and related classes
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JsonApiMeta {
}
