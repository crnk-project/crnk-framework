package io.crnk.core.engine.internal.information.resource;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProvider;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.information.resource.VersionRange;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.resource.ResourceTypeHolder;
import io.crnk.core.resource.annotations.JsonApiEmbeddable;
import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiVersion;
import io.crnk.core.resource.annotations.JsonIncludeStrategy;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.PatchStrategy;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * Process the Crnk JSON API annotations.
 */
public class DefaultResourceFieldInformationProvider implements ResourceFieldInformationProvider {


    @Override
    public Optional<Boolean> useFieldType(BeanAttributeInformation attributeDesc) {
        Field field = attributeDesc.getField();
        if (field != null) {
            for (Annotation annotation : field.getAnnotations()) {
                if (annotation.annotationType() == JsonApiId.class
                        || annotation.annotationType() == JsonApiRelation.class
                        || annotation.annotationType() == JsonApiField.class
                        || annotation.annotationType() == JsonApiMetaInformation.class
                        || annotation.annotationType() == JsonApiLinksInformation.class) {
                    return Optional.of(true);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<SerializeType> getSerializeType(BeanAttributeInformation attributeDesc) {

        Optional<JsonApiRelation> jsonApiRelation = attributeDesc.getAnnotation(JsonApiRelation.class);
        if (jsonApiRelation.isPresent()) {
            return Optional.of(jsonApiRelation.get().serialize());
        }
        return Optional.empty();
    }

    @Override
    public Optional<JsonIncludeStrategy> getJsonIncludeStrategy(BeanAttributeInformation attributeDesc) {
        return Optional.empty();
    }

    @Override
    public Optional<RelationshipRepositoryBehavior> getRelationshipRepositoryBehavior(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiRelation> jsonApiRelation = attributeDesc.getAnnotation(JsonApiRelation.class);
        if (jsonApiRelation.isPresent()) {
            return Optional.of(jsonApiRelation.get().repositoryBehavior());
        }
        return Optional.empty();
    }

    @Override
    public Optional<PatchStrategy> getPatchStrategy(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiField> annotation = attributeDesc.getAnnotation(JsonApiField.class);
        if (annotation.isPresent()) {
            return Optional.of(annotation.get().patchStrategy());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getMappedBy(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiRelation> jsonApiRelation = attributeDesc.getAnnotation(JsonApiRelation.class);
        if (jsonApiRelation.isPresent()) {
            return Optional.of(jsonApiRelation.get().mappedBy());
        }
        return Optional.empty();
    }

    @Override
    public Optional<VersionRange> getVersionRange(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiVersion> annotation = attributeDesc.getAnnotation(JsonApiVersion.class);
        if (annotation.isPresent()) {
            JsonApiVersion jsonApiVersion = annotation.get();
            return Optional.of(VersionRange.of(jsonApiVersion.min(), jsonApiVersion.max()));
        }
        return Optional.empty();
    }

    @Override
    public boolean isEmbeddedType(BeanAttributeInformation attributeDesc) {
        Class elementType = ClassUtils.getRawType(ClassUtils.getElementType(attributeDesc.getImplementationType()));
        return elementType.getAnnotation(JsonApiEmbeddable.class) != null;
    }

    @Override
    public Optional<LookupIncludeBehavior> getLookupIncludeBehavior(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiRelation> jsonApiRelation = attributeDesc.getAnnotation(JsonApiRelation.class);
        if (jsonApiRelation.isPresent()) {
            return Optional.of(jsonApiRelation.get().lookUp());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getOppositeName(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiRelation> jsonApiRelation = attributeDesc.getAnnotation(JsonApiRelation.class);
        if (jsonApiRelation.isPresent()) {
            return Optional.ofNullable(StringUtils.emptyToNull(jsonApiRelation.get().opposite()));
        }
        return Optional.empty();
    }


    @Override
    public void init(ResourceInformationProviderContext context) {
        // nothing to do
    }

    @Override
    public Optional<Boolean> isIgnored(BeanAttributeInformation attributeDesc) {
        if (attributeDesc.getName().equals(ResourceTypeHolder.TYPE_ATTRIBUTE) && ResourceTypeHolder.class.isAssignableFrom(attributeDesc.getBeanInformation().getImplementationClass())) {
            return Optional.of(true);
        }

        Field field = attributeDesc.getField();
        boolean isTransient = field != null && Modifier.isTransient(field.getModifiers());
        boolean relationshipIdField = attributeDesc.getAnnotation(JsonApiRelationId.class).isPresent();
        boolean idField = attributeDesc.getAnnotation(JsonApiId.class).isPresent();
        if (isTransient || relationshipIdField && !idField) {
            return Optional.of(true);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getJsonName(BeanAttributeInformation attributeDesc) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> isPostable(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiField> annotation = attributeDesc.getAnnotation(JsonApiField.class);
        if (annotation.isPresent()) {
            return Optional.of(annotation.get().postable());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> isDeletable(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiField> annotation = attributeDesc.getAnnotation(JsonApiField.class);
        if (annotation.isPresent()) {
            return Optional.of(annotation.get().deletable());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> isPatchable(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiField> annotation = attributeDesc.getAnnotation(JsonApiField.class);
        if (annotation.isPresent()) {
            return Optional.of(annotation.get().patchable());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> isReadable(final BeanAttributeInformation attributeDesc) {
        Optional<JsonApiField> annotation = attributeDesc.getAnnotation(JsonApiField.class);
        if (annotation.isPresent()) {
            return Optional.of(annotation.get().readable());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> isSortable(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiField> annotation = attributeDesc.getAnnotation(JsonApiField.class);
        if (annotation.isPresent()) {
            return Optional.of(annotation.get().sortable());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> isFilterable(BeanAttributeInformation attributeDesc) {
        Optional<JsonApiField> annotation = attributeDesc.getAnnotation(JsonApiField.class);
        if (annotation.isPresent()) {
            return Optional.of(annotation.get().filterable());
        }
        return Optional.empty();
    }

    @Override
    public Optional<ResourceFieldType> getFieldType(BeanAttributeInformation attributeDesc) {
        if (attributeDesc.getAnnotation(JsonApiId.class).isPresent()) {
            return Optional.of(ResourceFieldType.ID);
        }

        if (attributeDesc.getAnnotation(JsonApiRelation.class).isPresent()) {
            return Optional.of(ResourceFieldType.RELATIONSHIP);
        }

        if (attributeDesc.getAnnotation(JsonApiMetaInformation.class).isPresent()) {
            return Optional.of(ResourceFieldType.META_INFORMATION);
        }

        if (attributeDesc.getAnnotation(JsonApiLinksInformation.class).isPresent()) {
            return Optional.of(ResourceFieldType.LINKS_INFORMATION);
        }
        return Optional.empty();
    }
}
