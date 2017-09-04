package io.crnk.jpa.internal;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProviderBase;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.utils.Optional;

import javax.persistence.*;

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
		Optional<OneToMany> oneToMany = attributeDesc.getAnnotation(OneToMany.class);
		Optional<ManyToOne> manyToOne = attributeDesc.getAnnotation(ManyToOne.class);
		Optional<ManyToMany> manyToMany = attributeDesc.getAnnotation(ManyToMany.class);
		if (oneToMany.isPresent() || manyToOne.isPresent() || manyToMany.isPresent()) {
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
		Optional<OneToMany> oneToMany = attributeDesc.getAnnotation(OneToMany.class);
		if (oneToMany.isPresent()) {
			return Optional.ofNullable(StringUtils.emptyToNull(oneToMany.get().mappedBy()));
		}
		Optional<ManyToMany> manyToMany = attributeDesc.getAnnotation(ManyToMany.class);
		if (manyToMany.isPresent()) {
			return Optional.ofNullable(StringUtils.emptyToNull(manyToMany.get().mappedBy()));
		}
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

	private Optional<SerializeType> toSerializeType(FetchType fetch) {
		return Optional.of(fetch == FetchType.EAGER ? SerializeType.ONLY_ID : SerializeType.LAZY);
	}
}
