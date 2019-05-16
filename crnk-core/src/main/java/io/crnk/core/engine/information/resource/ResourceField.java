package io.crnk.core.engine.information.resource;

import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonIncludeStrategy;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.PatchStrategy;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.lang.reflect.Type;

public interface ResourceField {

    ResourceFieldType getResourceFieldType();

    /**
     * @return if this relationship field points to another owner with {@link JsonApiRelation#mappedBy()}.
     */
    boolean isMappedBy();

    /**
     * See also
     * {@link io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically}
     * }
     *
     * @return if lookup should be performed
     */
    LookupIncludeBehavior getLookupIncludeBehavior();

    /**
     * @return name of opposite attribute in case of a bidirectional relation.
     */
    String getOppositeName();

    /**
     * @return resourceType of the opposite resource in case of a relation.
     */
    String getOppositeResourceType();

    /**
     * @return name used in Json
     */
    String getJsonName();

    /**
     * @return name used in Java
     */
    String getUnderlyingName();

    Class<?> getType();

    Type getGenericType();

    SerializeType getSerializeType();

    JsonIncludeStrategy getJsonIncludeStrategy();

    /**
     * @return the non-collection type. Matches {@link #getType()} for
     * non-collections. Returns the type argument in case of a
     * collection type.
     */
    Class<?> getElementType();

    /**
     * @return resourceInformation this field belongs to.
     * @deprecated use {@link #getResourceInformation()}
     */
    @Deprecated
    ResourceInformation getParentResourceInformation();

    /**
     * @return resourceInformation this field belongs to.
     */
    ResourceInformation getResourceInformation();

    void setResourceInformation(ResourceInformation resourceInformation);

    boolean isCollection();

    /**
     * Allows to get and set the value of this field.
     */
    ResourceFieldAccessor getAccessor();

    /**
     * @return whether this relationship field is backed by an id Field.
     */
    boolean hasIdField();

    String getIdName();

    Class getIdType();

    ResourceFieldAccessor getIdAccessor();

    /**
     * @return access information for this resource (postable, patchable)
     */
    ResourceFieldAccess getAccess();

    RelationshipRepositoryBehavior getRelationshipRepositoryBehavior();

    PatchStrategy getPatchStrategy();

}