package io.crnk.core.engine.internal.information.resource;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.resource.annotations.JsonIncludeStrategy;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.PatchStrategy;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.lang.reflect.Type;
import java.util.Objects;

public class ResourceFieldImpl implements ResourceField {

    private String jsonName;

    private final String underlyingName;

    private final Class<?> type;

    private final Type genericType;

    private final SerializeType serializeType;

    private final JsonIncludeStrategy jsonIncludeStrategy;

    private final String oppositeResourceType;

    private LookupIncludeBehavior lookupIncludeBehavior;

    private ResourceFieldType resourceFieldType;

    private String oppositeName;

    private RelationshipRepositoryBehavior relationshipRepositoryBehavior;

    private ResourceInformation resourceInformation;

    private ResourceFieldAccessor accessor;

    private ResourceFieldAccess access;

    private String idName;

    private ResourceFieldAccessor idAccessor;

    private Class idType;

    private PatchStrategy patchStrategy;

    private boolean mappedBy;

    public ResourceFieldImpl(String jsonName, String underlyingName, ResourceFieldType resourceFieldType, Class<?> type,
                             Type genericType, String oppositeResourceType) {
        this(jsonName, underlyingName, resourceFieldType, type, genericType,
                oppositeResourceType, null, SerializeType.LAZY, JsonIncludeStrategy.DEFAULT, LookupIncludeBehavior.NONE,
                new ResourceFieldAccess(true, true, true, true, true, true),
                null, null, null, RelationshipRepositoryBehavior.DEFAULT, PatchStrategy.DEFAULT);
    }

    public ResourceFieldImpl(String jsonName, String underlyingName, ResourceFieldType resourceFieldType, Class<?> type,
                             Type genericType, String oppositeResourceType, String oppositeName, SerializeType serializeType,
                             JsonIncludeStrategy jsonIncludeStrategy, LookupIncludeBehavior lookupIncludeBehavior,
                             ResourceFieldAccess access, String idName, Class idType, ResourceFieldAccessor idAccessor,
                             RelationshipRepositoryBehavior relationshipRepositoryBehavior, PatchStrategy patchStrategy) {
        this.jsonName = jsonName;
        this.underlyingName = underlyingName;
        this.resourceFieldType = resourceFieldType;
        this.serializeType = serializeType;
        this.jsonIncludeStrategy = jsonIncludeStrategy;
        this.type = type;
        this.genericType = genericType;
        this.lookupIncludeBehavior = lookupIncludeBehavior;
        this.oppositeName = oppositeName;
        this.oppositeResourceType = oppositeResourceType;
        this.access = access;
        this.idName = idName;
        this.idType = idType;
        this.idAccessor = idAccessor;
        this.relationshipRepositoryBehavior = relationshipRepositoryBehavior;
        this.patchStrategy = patchStrategy;
    }

    public void setMappedBy(boolean mappedBy) {
        this.mappedBy = mappedBy;
    }

    @Deprecated
    public void setJsonName(String jsonName) {
        this.jsonName = jsonName;
    }

    @Deprecated
    public void setResourceFieldType(ResourceFieldType resourceFieldType) {
        this.resourceFieldType = resourceFieldType;
    }

    public ResourceFieldType getResourceFieldType() {
        return resourceFieldType;
    }

    @Override
    public boolean isMappedBy() {
        return mappedBy;
    }

    public RelationshipRepositoryBehavior getRelationshipRepositoryBehavior() {
        return relationshipRepositoryBehavior;
    }

    /**
     * See also
     * {@link io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically}
     * }
     *
     * @return if lookup should be performed
     */
    public LookupIncludeBehavior getLookupIncludeBehavior() {
        return lookupIncludeBehavior;
    }

    /**
     * @return name of opposite attribute in case of a bidirectional relation.
     */
    public String getOppositeName() {
        return oppositeName;
    }

    public String getJsonName() {
        return jsonName;
    }

    public String getUnderlyingName() {
        return underlyingName;
    }

    public String getOppositeResourceType() {
        PreconditionUtil.verifyEquals(ResourceFieldType.RELATIONSHIP, resourceFieldType, "field %s of %s is not an association",
                underlyingName, resourceInformation.getResourceType());
        if (getElementType() != Object.class) {
            PreconditionUtil.verify(oppositeResourceType != null, "field %s of %s does not have an opposite resource type",
                    underlyingName, resourceInformation.getResourceType());
        }
        return oppositeResourceType;
    }

    public Class<?> getType() {
        return type;
    }

    public Type getGenericType() {
        return genericType;
    }

    public SerializeType getSerializeType() {
        return serializeType;
    }


    public JsonIncludeStrategy getJsonIncludeStrategy() {
        return jsonIncludeStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceFieldImpl that = (ResourceFieldImpl) o;
        return Objects.equals(jsonName, that.jsonName) && resourceInformation == that.resourceInformation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonName, resourceInformation);
    }

    /**
     * Returns the non-collection type. Matches {@link #getType()} for
     * non-collections. Returns the type argument in case of a collection type.
     *
     * @return Ask Remmo
     */
    public Class<?> getElementType() {
        return ClassUtils.getRawType(ClassUtils.getElementType(genericType));
    }

    @Deprecated
    public ResourceInformation getParentResourceInformation() {
        return getResourceInformation();
    }

    @Override
    public ResourceInformation getResourceInformation() {
        return resourceInformation;
    }

    @Override
    public ResourceFieldAccessor getAccessor() {
        PreconditionUtil.verify(accessor != null, "field %s not properly initialized", underlyingName);
        return accessor;
    }

    @Override
    public boolean hasIdField() {
        assertRelationship();
        return idName != null;
    }

    @Override
    public String getIdName() {
        return idName;
    }

    @Override
    public Class getIdType() {
        assertRelationship();
        return idType;
    }

    @Override
    public ResourceFieldAccessor getIdAccessor() {
        assertRelationship();
        return idAccessor;
    }

    public void setIdField(String idName, Class idType, ResourceFieldAccessor idAccessor) {
        assertRelationship();
        // TODO to be eliminated by a builder pattern soon
        this.idName = idName;
        this.idType = idType;
        this.idAccessor = idAccessor;
    }

    private void assertRelationship() {
        PreconditionUtil
                .assertEquals("not available for non-relationship fields", ResourceFieldType.RELATIONSHIP, getResourceFieldType
                        ());
    }


    public void setAccessor(ResourceFieldAccessor accessor) {
        // TODO to be eliminated by a builder pattern soon
        this.accessor = accessor;
    }

    public void setResourceInformation(ResourceInformation resourceInformation) {
        if (this.accessor == null && resourceInformation.getResourceClass() == Resource.class) {
            this.accessor = new RawResourceFieldAccessor(underlyingName, resourceFieldType, type);
        } else if (this.accessor == null) {
            this.accessor = new ReflectionFieldAccessor(resourceInformation.getResourceClass(), underlyingName, type);
        }
        if (this.idAccessor == null && idName != null) {
            this.idAccessor = new ReflectionFieldAccessor(resourceInformation.getResourceClass(), idName, idType);
            if (idType == ResourceIdentifier.class) {
                this.idAccessor = new ResourceIdentifierAccessorAdapter(idAccessor);
            }
        }
        this.resourceInformation = resourceInformation;
    }

    public void setRelationshipRepositoryBehavior(RelationshipRepositoryBehavior relationshipRepositoryBehavior) {
        this.relationshipRepositoryBehavior = relationshipRepositoryBehavior;
    }

    public void setLookupIncludeBehavior(LookupIncludeBehavior behavior) {
        this.lookupIncludeBehavior = behavior;
    }

    public void setOppositeName(String oppositeName) {
        this.oppositeName = oppositeName;
    }

    public void setAccess(ResourceFieldAccess access) {
        this.access = access;
    }

    static class ResourceFieldAccessorWrapper implements ResourceFieldAccessor {

        protected final ResourceFieldAccessor wrappedAccessor;

        public ResourceFieldAccessorWrapper(ResourceFieldAccessor wrappedAccessor) {
            this.wrappedAccessor = wrappedAccessor;
        }

        @Override
        public Object getValue(Object resource) {
            return wrappedAccessor.getValue(resource);
        }

        @Override
        public void setValue(Object resource, Object fieldValue) {
            wrappedAccessor.setValue(resource, fieldValue);
        }

        @Override
        public Class getImplementationClass() {
            return wrappedAccessor.getImplementationClass();
        }
    }

    class ResourceIdentifierAccessorAdapter extends ResourceFieldAccessorWrapper {

        public ResourceIdentifierAccessorAdapter(ResourceFieldAccessor idAccessor) {
            super(idAccessor);
        }

        @Override
        public void setValue(Object resource, Object fieldValue) {
            if (fieldValue == null || fieldValue instanceof ResourceIdentifier) {
                super.setValue(resource, fieldValue);
            } else {
                // TODO try to get access to opposite ResourceInformation in the future, ok for basic use cases
                super.setValue(resource, new ResourceIdentifier(fieldValue.toString(), oppositeResourceType));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[");
        if (resourceInformation != null && resourceInformation.getResourceClass() != null) {
            sb.append("resourceClass=").append(resourceInformation.getResourceClass().getName());
        }
        sb.append(", name=").append(underlyingName);
        if (resourceInformation != null && resourceInformation.getResourceType() != null) {
            sb.append(",resourceType=").append(resourceInformation.getResourceType());
        }
        sb.append("]");
        return sb.toString();
    }

    public boolean isCollection() {
        return Iterable.class.isAssignableFrom(getType());
    }

    @Override
    public ResourceFieldAccess getAccess() {
        return access;
    }

    @Override
    public PatchStrategy getPatchStrategy() {
        return patchStrategy;
    }
}