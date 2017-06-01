package io.crnk.legacy.repository.annotations;

import java.io.Serializable;
import java.lang.annotation.*;

/**
 * <p>
 * Method annotated with this annotation will be used to set relationship repository to a particular resource. The method
 * must be defined in a class annotated with {@link JsonApiRelationshipRepository}.
 * </p>
 * <p>
 * The requirements for the method parameters are as follows:
 * </p>
 * <ol>
 * <li>Instance of a source resource</li>
 * <li>Instance of a relationship to be set</li>
 * <li>Relationship's filed name</li>
 * </ol>
 * <p>
 * The method's return value should be <i>void</i>.
 * </p>
 *
 * @see io.crnk.legacy.repository.RelationshipRepository#setRelation(Object, Serializable, String)
 * @deprecated Make use of ResourceRepositoryV2 and related classes
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Deprecated
public @interface JsonApiSetRelation {
}
