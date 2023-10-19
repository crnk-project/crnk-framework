package io.crnk.data.jpa.internal;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProviderBase;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.resource.annotations.SerializeType;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import java.util.Optional;

public class JpaResourceFieldInformationProvider extends ResourceFieldInformationProviderBase {


    @Override
    public Optional<Boolean> isSortable(BeanAttributeInformation attributeDesc) {
        Optional<Lob> lob = attributeDesc.getAnnotation(Lob.class);
        if (lob.isPresent()) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> isFilterable(BeanAttributeInformation attributeDesc) {
        Optional<Lob> lob = attributeDesc.getAnnotation(Lob.class);
        if (lob.isPresent()) {
            return Optional.of(false);
        }
        return Optional.empty();
    }


    @Override
    public Optional<Boolean> isPostable(BeanAttributeInformation attributeDesc) {
        Optional<Column> column = attributeDesc.getAnnotation(Column.class);
        Optional<Version> version = attributeDesc.getAnnotation(Version.class);
        if (!version.isPresent() && column.isPresent()) {
            return Optional.of(column.get().insertable());
        }
        Optional<GeneratedValue> generatedValue = attributeDesc.getAnnotation(GeneratedValue.class);
        if (generatedValue.isPresent()) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    @Override
    public boolean isEmbeddedType(BeanAttributeInformation attributeDesc) {
        Class elementType = ClassUtils.getRawType(ClassUtils.getElementType(attributeDesc.getImplementationType()));
        return elementType.getAnnotation(Embeddable.class) != null;
    }

    @Override
    public Optional<Boolean> isPatchable(BeanAttributeInformation attributeDesc) {
        Optional<Column> column = attributeDesc.getAnnotation(Column.class);
        Optional<Version> version = attributeDesc.getAnnotation(Version.class);
        if (!version.isPresent() && column.isPresent()) {
            return Optional.of(column.get().updatable());
        }
        Optional<GeneratedValue> generatedValue = attributeDesc.getAnnotation(GeneratedValue.class);
        if (generatedValue.isPresent()) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ResourceFieldType> getFieldType(BeanAttributeInformation attributeDesc) {
        Optional<OneToOne> oneToOne = attributeDesc.getAnnotation(OneToOne.class);
        Optional<OneToMany> oneToMany = attributeDesc.getAnnotation(OneToMany.class);
        Optional<ManyToOne> manyToOne = attributeDesc.getAnnotation(ManyToOne.class);
        Optional<ManyToMany> manyToMany = attributeDesc.getAnnotation(ManyToMany.class);
        if (oneToOne.isPresent() || oneToMany.isPresent() || manyToOne.isPresent() || manyToMany.isPresent()) {
            return Optional.of(ResourceFieldType.RELATIONSHIP);
        }

        Optional<Id> id = attributeDesc.getAnnotation(Id.class);
        Optional<EmbeddedId> embeddedId = attributeDesc.getAnnotation(EmbeddedId.class);
        if (id.isPresent() || embeddedId.isPresent()) {
            return Optional.of(ResourceFieldType.ID);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getOppositeName(BeanAttributeInformation attributeDesc) {
        return Optional.empty();
    }

    @Override
    public Optional<SerializeType> getSerializeType(BeanAttributeInformation attributeDesc) {
        Optional<OneToMany> oneToMany = attributeDesc.getAnnotation(OneToMany.class);
        if (oneToMany.isPresent()) {
            return toSerializeType(oneToMany.get().fetch());
        }
        Optional<ManyToOne> manyToOne = attributeDesc.getAnnotation(ManyToOne.class);
        if (manyToOne.isPresent()) {
            return toSerializeType(manyToOne.get().fetch());
        }
        Optional<ManyToMany> manyToMany = attributeDesc.getAnnotation(ManyToMany.class);
        if (manyToMany.isPresent()) {
            return toSerializeType(manyToMany.get().fetch());
        }
        Optional<ElementCollection> elementCollection = attributeDesc.getAnnotation(ElementCollection.class);
        if (elementCollection.isPresent()) {
            return toSerializeType(elementCollection.get().fetch());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getMappedBy(BeanAttributeInformation attributeDesc) {
        Optional<OneToMany> oneToMany = attributeDesc.getAnnotation(OneToMany.class);
        if (oneToMany.isPresent()) {
            return Optional.of(oneToMany.get().mappedBy());
        }

        Optional<OneToOne> oneToOne = attributeDesc.getAnnotation(OneToOne.class);
        if (oneToOne.isPresent()) {
            return Optional.of(oneToOne.get().mappedBy());
        }

        Optional<ManyToMany> manyToMany = attributeDesc.getAnnotation(ManyToMany.class);
        if (manyToMany.isPresent()) {
            return Optional.of(manyToMany.get().mappedBy());
        }
        return Optional.empty();
    }

    private Optional<SerializeType> toSerializeType(FetchType fetch) {
        return Optional.of(fetch == FetchType.EAGER ? SerializeType.ONLY_ID : SerializeType.LAZY);
    }
}
