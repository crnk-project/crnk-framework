package io.crnk.core.resource.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates holding a single or collection of ids of a relationship. This can give an number of performance and usability
 * benefits:
 * <p>
 * <ul>
 * <li>relationships can be set and updated without having to fetch and work with the related resources</li>
 * <li>SerializeType.ONLY_ID is efficently supported</li>
 * <li>A relationship repository must not be implemented. But Crnk can directly access the opposite resource repository.</li>
 * </ul>
 *
 * @JsonApiRelationId is well suited for data models where the id is directly available, while fetching the entire, related
 * resources is expensive.
 * <p>
 * A field type can either by a primitive (matching the resource id) or ${@link io.crnk.core.engine.document.ResourceIdentifier}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiRelationId {


}
