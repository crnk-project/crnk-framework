package io.crnk.core.engine.information.resource;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.utils.Optional;

public class ResourceFieldInformationProviderBase implements ResourceFieldInformationProvider {

	protected ResourceInformationProviderContext context;

	@Override
	public void init(ResourceInformationProviderContext context) {
		this.context = context;
	}

	@Override
	public Optional<Boolean> isIgnored(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<String> getJsonName(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> isPostable(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> isPatchable(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> isReadable(final BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> isSortable(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> isFilterable(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<ResourceFieldType> getFieldType(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<String> getOppositeName(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> useFieldType(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<LookupIncludeBehavior> getLookupIncludeBehavior(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}

	@Override
	public Optional<SerializeType> getSerializeType(BeanAttributeInformation attributeDesc) {
		return Optional.empty();
	}
}
