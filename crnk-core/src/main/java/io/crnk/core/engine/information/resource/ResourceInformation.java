package io.crnk.core.engine.information.resource;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInstanceBuilder;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.StringMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.exception.InvalidResourceException;
import io.crnk.core.exception.ResourceDuplicateIdException;
import io.crnk.core.exception.ResourceException;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds information about the type of the resource.
 */
public class ResourceInformation extends BeanInformationBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceInformation.class);


    private ResourceField parentField;

    /**
     * Found field of the id. Each resource has to contain a field marked by
     * JsonApiId annotation.
     */
    private ResourceField idField;

    /**
     * A set of fields that contains non-standard Java types (List, Set, custom
     * classes, ...).
     */
    private List<ResourceField> relationshipFields;

    /**
     * An underlying field's name which contains meta information about for a
     * resource
     */
    private ResourceField metaField;

    /**
     * An underlying field's name which contain links information about for a
     * resource
     */
    private ResourceField linksField;

    /**
     * Type name of the resource. Corresponds to {@link JsonApiResource#type}
     * for annotated resources.
     */
    private final String resourceType;

    /**
     * Type url path of the resource. Corresponds to {@link JsonApiResource#resourcePath}
     * for annotated resources.
     */
    private String resourcePath;

    /**
     * Creates a new instance of the given resource.
     */
    private ResourceInstanceBuilder<?> instanceBuilder;

    private final TypeParser parser;

    /**
     * Resource type of the super type.
     */
    private String superResourceType;

    private AnyResourceFieldAccessor anyFieldAccessor;

    private ResourceValidator validator;

    private Class<? extends PagingSpec> pagingSpecType;

    private ResourceFieldAccess access = new ResourceFieldAccess(true, true, true, true, true, true);

    private StringMapper idStringMapper = new StringMapper() {
        @Override
        public String toString(Object input) {
            return parser.toString(input);
        }

        @Override
        public Object parse(String input) {
            Class idType = getIdField().getType();
            return parser.parse(input, idType);
        }
    };

    private ResourceFieldAccessor childIdAccessor;

    private ResourceFieldAccessor parentIdAccessor;

    private boolean singularNesting;

    private VersionRange versionRange = VersionRange.UNBOUNDED;

    public ResourceInformation(TypeParser parser, Type implementationType, String resourceType, String superResourceType,
                               List<ResourceField> fields, Class<? extends PagingSpec> pagingSpecType) {
        this(parser, implementationType, resourceType, null, superResourceType, null, fields, pagingSpecType);
    }

    public ResourceInformation(TypeParser parser, Type implementationType, String resourceType, String resourcePath,
                               String superResourceType,
                               List<ResourceField> fields, Class<? extends PagingSpec> pagingSpecType) {
        this(parser, implementationType, resourceType, resourcePath, superResourceType, null, fields, pagingSpecType);
    }

    public ResourceInformation(TypeParser parser, Type implementationType, String resourceType, String superResourceType,
                               ResourceInstanceBuilder<?> instanceBuilder, List<ResourceField> fields, Class<? extends PagingSpec> pagingSpecType) {
        this(parser, implementationType, resourceType, null, superResourceType, instanceBuilder, fields, pagingSpecType);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ResourceInformation(TypeParser parser, Type implementationType, String resourceType, String resourcePath,
                               String superResourceType,
                               ResourceInstanceBuilder<?> instanceBuilder, List<ResourceField> fields, Class<? extends PagingSpec> pagingSpecType) {
        super(implementationType, fields);
        this.parser = parser;
        this.resourceType = resourceType;
        this.resourcePath = resourcePath;
        this.superResourceType = superResourceType;
        this.instanceBuilder = instanceBuilder;

        this.pagingSpecType = pagingSpecType;

        initFields();
        if (this.instanceBuilder == null) {
            this.instanceBuilder = new DefaultResourceInstanceBuilder(implementationClass);
        }

        initAny();
    }

    public ResourceFieldAccess getAccess() {
        return access;
    }

    public void setAccess(ResourceFieldAccess access) {
        this.access = access;
    }

    private boolean shouldBeNested() {
        JsonApiResource annotation = implementationClass.getAnnotation(JsonApiResource.class);
        return annotation != null && annotation.nested();
    }

    /**
     * @JsonApiResource(nested=true) together with a idField that is a @JsonApiRelationId to the parent.
     */
    private void setupOneNesting() {
        String idName = idField.getUnderlyingName();
        for (ResourceField relationshipField : relationshipFields) {
            if (relationshipField.hasIdField() && idName.equals(relationshipField.getIdName())) {
                parentField = relationshipField;
                break;
            }
        }

        PreconditionUtil.verify(parentField != null, "resource %s is marked as nested but no parent relationship found. Since the " +
				"resource is singular, it carries the same identifier as its parent (stored in the `@JsonApiId` field). To point to the parent, make sure " +
				" the relationship is annotated with @JsonApiRelation(idField=<idField>).", this);
        childIdAccessor = new ResourceFieldAccessor() {

            @Override
            public Object getValue(Object object) {
                if (idField.getType().isInstance(object)) {
                    // parent and child have same ID
                    return object;
                }
                return idField.getAccessor().getValue(object);
            }

            @Override
            public void setValue(Object resource, Object fieldValue) {
                if (idField.getType().isInstance(resource)) {
                    // parent and child have same ID, cannot pass ID as resource
                    throw new UnsupportedOperationException();
                }
                idField.getAccessor().getValue(fieldValue);
            }

            @Override
            public Class getImplementationClass() {
                return idField.getType();
            }
        };
        parentIdAccessor = childIdAccessor;
        singularNesting = true;
    }

    /**
     * in the future @JsonApiResource(nested=true), currently just structured id field with local and parent id.
     */
    private boolean setupManyNesting() {
        BeanAttributeInformation parentAttribute = null;
        BeanAttributeInformation idAttribute = null;
        BeanInformation beanInformation = BeanInformation.get(idField.getType());
        for (String attributeName : beanInformation.getAttributeNames()) {
            BeanAttributeInformation attribute = beanInformation.getAttribute(attributeName);
            if (attribute.getAnnotation(JsonApiRelationId.class).isPresent()) {
                PreconditionUtil.verify(parentAttribute == null,
                        "nested identifiers can only have a single @JsonApiRelationId annotated field, got multiple for %s",
                        beanInformation.getImplementationClass());
                parentAttribute = attribute;
            } else if (attribute.getAnnotation(JsonApiId.class).isPresent()) {
                PreconditionUtil.verify(idAttribute == null,
                        "nested identifiers can only one attribute being annotated with @JsonApiId, got multiple for %s",
                        beanInformation.getImplementationClass());
                idAttribute = attribute;
            }
        }

        if (parentAttribute != null || idAttribute != null) {
            if (!shouldBeNested()) {
                LOGGER.warn("add @JsonApiResource(nested=true) to {} to mark it as being nested, in the future automatic discovery based on the id will be removed",
                        implementationClass);
            }

            PreconditionUtil.verify(idAttribute != null,
                    "nested identifiers must have attribute annotated with @JsonApiId, got none for %s",
                    beanInformation.getImplementationClass());
            PreconditionUtil.verify(parentAttribute != null,
                    "nested identifiers must have attribute annotated with @JsonApiRelationId, got none for %s",
                    beanInformation.getImplementationClass());
            String relationshipName = parentAttribute.getName().substring(0, parentAttribute.getName().length() - 2);

            // accessors for nested and parent id, able to deal with both resources and identifiers as parameters
            this.parentIdAccessor = new NestedIdAccessor(parentAttribute);
            this.childIdAccessor = new NestedIdAccessor(idAttribute);

            // check whether parentField is duplicated in ID. This can be a valid use case if the nested identifier is considered
            // an add-on to an existing object.
            String parentName = parentAttribute.getName();
            Optional<ResourceField> optParentField = relationshipFields.stream().filter(it -> it.hasIdField() && it.getIdName().equals(parentName)).findFirst();
            if (optParentField.isPresent()) {
                parentField = optParentField.get();
            } else {
                PreconditionUtil.verify(parentAttribute.getName().endsWith("Id"),
                        "nested identifier must have @JsonApiRelationId field being named with a 'Id' suffix or match in name with a @JsonApiRelationId annotated field on the resource, got %s for "
                                + "%s",
                        parentAttribute.getName(), beanInformation.getImplementationClass());

                parentField = findFieldByUnderlyingName(relationshipName);
                PreconditionUtil.verify(parentField != null,
                        "naming of relationship to parent resource and relationship identifier within resource identifier must "
                                + "match, not found for %s of %s",
                        parentAttribute.getName(), implementationClass);

                ((ResourceFieldImpl) parentField).setIdField(parentAttribute.getName(), parentAttribute.getImplementationClass(), parentIdAccessor);
            }

            return true;
        }
        return false;
    }

    /**
     * Hide with implementation/interface
     */
    @Deprecated
    public void initNesting() {
        boolean nested = setupManyNesting();
        if (!nested && shouldBeNested()) {
            setupOneNesting();
        }
        if (isNested()) {
            PreconditionUtil.verify(parentField.getOppositeName() != null,
                    "relationship between parent pointing to a nested resource must specify @JsonApiRelation.mappedBy or @JsonApiRelation.opposite, not "
                            + "found for '%s' of %s",
                    parentField.getUnderlyingName(), implementationClass);
        }
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    class NestedIdAccessor implements ResourceFieldAccessor {

        private final BeanAttributeInformation nestedField;

        protected NestedIdAccessor(BeanAttributeInformation nestedField) {
            this.nestedField = nestedField;
        }

        @Override
        public Object getValue(Object object) {
            if (idField.getType().isInstance(object)) {
                return nestedField.getValue(object);
            }
            Object id = getIdField().getAccessor().getValue(object);
            return nestedField.getValue(id);
        }

        @Override
        public void setValue(Object object, Object fieldValue) {
            if (idField.getType().isInstance(object)) {
                nestedField.setValue(object, fieldValue);
            } else {
                Object id = getIdField().getAccessor().getValue(object);
                nestedField.setValue(id, fieldValue);
            }
        }

        @Override
        public Class getImplementationClass() {
            return nestedField.getImplementationClass();
        }
    }

    @Deprecated
    public void setValidator(ResourceValidator validator) {
        this.validator = validator;
    }

    @Deprecated
    public ResourceValidator getValidator() {
        return validator;
    }

    @Deprecated
    public void setIdStringMapper(StringMapper idStringMapper) {
        this.idStringMapper = idStringMapper;
    }

    public StringMapper getIdStringMapper() {
        return idStringMapper;
    }

    public AnyResourceFieldAccessor getAnyFieldAccessor() {
        return anyFieldAccessor;
    }

    private void initAny() {
        final Method jsonAnyGetter = ClassUtils.findMethodWith(implementationClass, JsonAnyGetter.class);
        final Method jsonAnySetter = ClassUtils.findMethodWith(implementationClass, JsonAnySetter.class);

        if (absentAnySetter(jsonAnyGetter, jsonAnySetter)) {
            throw new InvalidResourceException(
                    String.format("A resource %s has to have both methods annotated with @JsonAnySetter and @JsonAnyGetter",
                            implementationClass.getCanonicalName()));
        }

        if (jsonAnyGetter != null) {
            anyFieldAccessor = new AnyResourceFieldAccessor() {

                @Override
                @SuppressWarnings("unchecked")
                public Map<String, Object> getValues(Object resource) {
                    try {
                        Object o = jsonAnyGetter.invoke(resource);
                        return (Map<String, Object>) o;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new ResourceException(
                                String.format("Exception while reading %s due to %s", resource, e.getMessage()), e);
                    }
                }

                @Override
                public void setValue(Object resource, String name, Object fieldValue) {
                    try {
                        jsonAnySetter.invoke(resource, name, fieldValue);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new ResourceException(
                                String.format("Exception while writting %s.%s=%s due to %s", resource, name, fieldValue,
                                        e.getMessage()), e);
                    }
                }
            };
        }
    }

    /**
     * The resource has to have both method annotated with {@link JsonAnySetter} and {@link JsonAnyGetter} to allow
     * proper handling.
     *
     * @return <i>true</i> if resource definition is incomplete, <i>false</i> otherwise
     */
    private static boolean absentAnySetter(Method jsonAnyGetter, Method jsonAnySetter) {
        return (jsonAnySetter == null && jsonAnyGetter != null) ||
                (jsonAnySetter != null && jsonAnyGetter == null);
    }

    protected void initFields() {
        super.initFields();

        if (fields != null) {
            List<ResourceField> idFields = ResourceFieldType.ID.filter(fields);
            if (idFields.size() > 1) {
                throw new ResourceDuplicateIdException(implementationClass.getCanonicalName());
            }

            this.idField = idFields.isEmpty() ? null : idFields.get(0);
            this.relationshipFields = ResourceFieldType.RELATIONSHIP.filter(fields);
            this.metaField = getField(implementationClass, ResourceFieldType.META_INFORMATION, fields);
            this.linksField = getField(implementationClass, ResourceFieldType.LINKS_INFORMATION, fields);
        } else {
            this.relationshipFields = Collections.emptyList();
            this.metaField = null;
            this.linksField = null;
            this.idField = null;
        }
    }

    @Override
    protected void initField(ResourceField resourceField) {
        resourceField.setResourceInformation(this);
        super.initField(resourceField);
    }


    private static <T> ResourceField getField(Class<T> resourceClass, ResourceFieldType type, Collection<ResourceField> classFields) {
        List<ResourceField> matches = new ArrayList<>(1);
        for (ResourceField field : classFields) {
            if (field.getResourceFieldType() == type) {
                matches.add(field);
            }
        }

        if (matches.isEmpty()) {
            return null;
        } else if (matches.size() > 1) {
            throw new IllegalStateException("multiple " + type + " fields for + " + resourceClass.getCanonicalName());
        }
        return matches.get(0);
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourcePath() {
        if (resourcePath == null) {
            return resourceType;
        }
        return resourcePath;
    }

    public String getSuperResourceType() {
        return superResourceType;
    }

    public <T> ResourceInstanceBuilder<T> getInstanceBuilder() {
        return (ResourceInstanceBuilder<T>) instanceBuilder;
    }

    /**
     * @Deprecated use {@link #getImplementationClass()}
     */
    public Class<?> getResourceClass() {
        return getImplementationClass();
    }

    public ResourceField getIdField() {
        return idField;
    }

    public List<ResourceField> getRelationshipFields() {
        return relationshipFields;
    }

    /**
     * @deprecated use {@link #findFieldByJsonName(String, int)} with version instead.
     */
    @Deprecated
    public ResourceField findRelationshipFieldByName(String name) {
        ResourceField resourceField = findFieldByName(name);
        return resourceField != null && resourceField.getResourceFieldType() == ResourceFieldType.RELATIONSHIP ? resourceField : null;
    }

    public ResourceField getMetaField() {
        return metaField;
    }

    public ResourceField getLinksField() {
        return linksField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceInformation that = (ResourceInformation) o;
        return Objects.equals(implementationClass, that.implementationClass) && Objects.equals(resourceType, that.resourceType)
                && Objects.equals(resourcePath, that.resourcePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceType);
    }

    /**
     * Converts the given id to a string.
     *
     * @param id id
     * @return stringified id
     */
    public String toIdString(Object id) {
        if (id == null) {
            return null;
        }
        return idStringMapper.toString(id);
    }

    /**
     * @param resourceOrId resource or id object
     * @return ResourceIdentifier of that resource
     */
    public ResourceIdentifier toResourceIdentifier(Object resourceOrId) {
        if (resourceOrId == null) {
            return null;
        }
        if (resourceOrId instanceof Resource) {
            return ((Resource) resourceOrId).toIdentifier();
        }
        if (implementationClass.isInstance(resourceOrId)) {
            resourceOrId = getId(resourceOrId);
        }
        if (resourceOrId instanceof ResourceIdentifier) {
            return (ResourceIdentifier) resourceOrId;
        }
        String strId;
        if (resourceOrId instanceof String) {
            strId = (String) resourceOrId;
        } else {
            strId = toIdString(resourceOrId);
        }
        return new ResourceIdentifier(strId, getResourceType());

    }

    /**
     * Converts the given id string into its object representation.
     *
     * @param id stringified id
     * @return id
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Serializable parseIdString(String id) {
        if (id == null) {
            return null;
        }
        return (Serializable) idStringMapper.parse(id);
    }

    /**
     * @return id of the resource
     */
    public Object getId(Object resource) {
        return idField.getAccessor().getValue(resource);
    }

    public void setId(Object resource, Object id) {
        idField.getAccessor().setValue(resource, id);
    }

    public void verify(Object resource, Document requestDocument) {
        // nothing to do
        if (validator != null) {
            validator.validate(resource, requestDocument);
        }
    }

    public Class<? extends PagingSpec> getPagingSpecType() {
        return pagingSpecType;
    }

    /**
     * @return true if this resource is a child of another resource. This will result in nested URLs like /api/foo/1/bar/2 (multi-valued)
     * or /api/foo/1/bar (single-valued) for the nested bar resource.
     * The resource may still or may not be accessible from /api/bar/2 depending on @JsonApiExposed.
     */
    public boolean isNested() {
        return parentField != null;
    }

    /**
     * @return true if this nested resource has a 1:1 mapping to its parent.
     */
    public boolean isSingularNesting() {
        PreconditionUtil.verify(isNested(), "not a nested resource");
        return singularNesting;
    }


    /**
     * @return resource field pointing to the parent this resource belongs to.
     */
    public ResourceField getParentField() {
        PreconditionUtil.verify(parentField != null, "not a nested resource, cannot access parent field");
        return parentField;
    }

    /**
     * @return Allows to get child ID from a nested resource or its ID.
     */
    public ResourceFieldAccessor getChildIdAccessor() {
        PreconditionUtil.verify(isNested(), "not a nested resource, cannot access nested id accessor");
        return childIdAccessor;
    }

    /**
     * @return Allows to get identifier of parent resource from a nested resource or its ID.
     */
    public ResourceFieldAccessor getParentIdAccessor() {
        PreconditionUtil.verify(isNested(), "not a nested resource, cannot access nested id accessor");
        return parentIdAccessor;
    }

    /**
     * @return version range this field is applicable to. See also {@link @JsonApiVersion}
     */
    public VersionRange getVersionRange() {
        return versionRange;
    }

    public void setVersionRange(VersionRange versionRange) {
        this.versionRange = versionRange;
    }
}