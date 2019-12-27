package io.crnk.core.engine.information.resource;

import io.crnk.core.engine.internal.utils.ClassUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanInformationBase {

    protected final Class<?> implementationClass;

    protected final Type implementationType;

    /**
     * A set of resource's attribute fields.
     */
    private List<ResourceField> attributeFields;

    protected List<ResourceField> fields;

    private Map<String, List<ResourceField>> fieldVersionsByJsonName = new HashMap<>();

    private Map<String, ResourceField> fieldByUnderlyingName = new HashMap<>();

    private Map<String, ResourceFieldAccessor> fieldAccessors = new HashMap<>();

    public BeanInformationBase(Type implementationType, List<ResourceField> fields) {
        this.implementationClass = ClassUtils.getRawType(implementationType);
        this.implementationType = implementationType;
        this.fields = fields;
    }

    public ResourceField findFieldByUnderlyingName(String name) {
        return fieldByUnderlyingName.get(name);
    }

    public boolean hasJsonField(String jsonName) {
        return fieldVersionsByJsonName.containsKey(jsonName);
    }

    public List<ResourceField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public Type getImplementationType() {
        return implementationType;
    }

    public Class<?> getImplementationClass() {
        return implementationClass;
    }

    public List<ResourceField> getAttributeFields() {
        return attributeFields;
    }

    /**
     * @return field that matches the request jsonName and version.
     */
    public ResourceField findFieldByJsonName(String jsonName, int version) {
        List<ResourceField> fieldVersions = fieldVersionsByJsonName.get(jsonName);
        if (fieldVersions != null) {
            for (ResourceField field : fieldVersions) {
                if (field.getVersionRange().contains(version)) {
                    return field;
                }
            }
        }
        return null;
    }

    /**
     * @deprecated use {@link #findFieldByJsonName(String, int)} with version instead.
     */
    @Deprecated
    public ResourceField findFieldByName(String name) {
        return findFieldByJsonName(name, Integer.MAX_VALUE);
    }


    /**
     * @deprecated use {@link #findFieldByJsonName(String, int)} with version instead.
     */
    @Deprecated
    public ResourceField findAttributeFieldByName(String name) {
        ResourceField resourceField = findFieldByName(name);
        return resourceField != null && resourceField.getResourceFieldType() == ResourceFieldType.ATTRIBUTE ? resourceField : null;
    }

    protected void initFields() {
        if (fields != null) {
            this.attributeFields = ResourceFieldType.ATTRIBUTE.filter(fields);

            for (ResourceField resourceField : fields) {
                initField(resourceField);
            }
        } else {
            this.fieldAccessors = null;
            this.attributeFields = Collections.emptyList();
        }
    }

    protected void initField(ResourceField resourceField) {
        String jsonName = resourceField.getJsonName();
        List<ResourceField> list = fieldVersionsByJsonName.getOrDefault(jsonName, new ArrayList<>());
        list.add(resourceField);
        fieldVersionsByJsonName.put(jsonName, list);

        fieldByUnderlyingName.put(resourceField.getUnderlyingName(), resourceField);

        fieldAccessors.put(resourceField.getUnderlyingName(), resourceField.getAccessor());
        if (resourceField.getResourceFieldType() == ResourceFieldType.RELATIONSHIP && resourceField.hasIdField()) {
            fieldAccessors.put(resourceField.getIdName(), resourceField.getIdAccessor());

            // a non-JsonApiRelationId field can also be referenced => has its own ResourceField instance
            if (!fieldByUnderlyingName.containsKey(resourceField.getIdName())) {
                fieldByUnderlyingName.put(resourceField.getIdName(), resourceField);
            }
        }
    }


    @Deprecated
    public void setFields(List<ResourceField> fields) {
        this.fields = fields;
        this.initFields();
    }

    /**
     * @param name name of regular field or field annotated with @JsonApiRelationId
     * @return ResourceFieldAccessor to get and set values
     */
    public ResourceFieldAccessor getAccessor(String name) {
        return fieldAccessors.get(name);
    }
}

