package io.crnk.core.engine.information.resource;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import io.crnk.core.utils.Optional;

/**
 * Provides information necessary to properly serializer (or skip) resource attributes.
 *
 * @author Craig Setera, Remo Meier
 */
public interface ResourceFieldInformationProvider {

	void init(ResourceInformationProviderContext context);

	/**
	 * Return a boolean indicating whether the specified field should be ignored when doing serialization.
	 */
	Optional<Boolean> isIgnored(BeanAttributeInformation attributeDesc);

	/**
	 * Returns the name used for JSON serialization.
	 */
	Optional<String> getJsonName(BeanAttributeInformation attributeDesc);

	/**
	 * Returns whether they field can be modified upon a POST request.
	 */
	Optional<Boolean> isPostable(BeanAttributeInformation attributeDesc);

	/**
	 * Returns whether they field can be modified upon a PATCH request.
	 */
	Optional<Boolean> isPatchable(BeanAttributeInformation attributeDesc);

	/**
	 * Returns whether they field can be read.
	 */
	Optional<Boolean> isReadable(BeanAttributeInformation attributeDesc);

	/**
	 * Returns whether they field can be sorted.
	 */
	Optional<Boolean> isSortable(BeanAttributeInformation attributeDesc);

	/**
	 * Returns whether they field can be filtered.
	 */
	Optional<Boolean> isFilterable(BeanAttributeInformation attributeDesc);

	/**
	 * Returns they type of the field (ID, ATTRIBUTE, META, etc.)
	 */
	Optional<ResourceFieldType> getFieldType(BeanAttributeInformation attributeDesc);

	/**
	 * Returns for relationships the name of the opposite attribute if available.
	 */
	Optional<String> getOppositeName(BeanAttributeInformation attributeDesc);

	/**
	 * Returns true if the Java type of the field rather than the type of the method should be used.
	 * Usually determined by the presence of annotations.
	 */
	Optional<Boolean> useFieldType(BeanAttributeInformation attributeDesc);

	/**
	 * Returns the LookupIncludeBehavior to use.
	 */
	Optional<LookupIncludeBehavior> getLookupIncludeBehavior(BeanAttributeInformation attributeDesc);

	/**
	 * Returns the SerializeType to use.
	 */
	Optional<SerializeType> getSerializeType(BeanAttributeInformation attributeDesc);

	/**
	 * Returns the RelationshipRepositoryBehavior to use.
	 */
	Optional<RelationshipRepositoryBehavior> getRelationshipRepositoryBehavior(BeanAttributeInformation attributeDesc);
}
