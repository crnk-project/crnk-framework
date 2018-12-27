package io.crnk.core.engine.internal.information.resource;

import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.resource.ResourceFieldInformationProvider;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformationProviderContext;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.PatchStrategy;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.utils.Optional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
						|| annotation.annotationType() == JsonApiToOne.class
						|| annotation.annotationType() == JsonApiField.class
						|| annotation.annotationType() == JsonApiToMany.class
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

		Optional<JsonApiIncludeByDefault> jsonApiIncludeByDefault = attributeDesc.getAnnotation(JsonApiIncludeByDefault.class);
		if (jsonApiIncludeByDefault.isPresent()) {
			return Optional.of(SerializeType.EAGER);
		}

		Optional<JsonApiToMany> jsonApiToMany = attributeDesc.getAnnotation(JsonApiToMany.class);
		if (jsonApiToMany.isPresent() && !jsonApiToMany.get().lazy()) {
			return Optional.of(SerializeType.ONLY_ID);
		}

		Optional<JsonApiToOne> jsonApiToOne = attributeDesc.getAnnotation(JsonApiToOne.class);
		if (jsonApiToOne.isPresent() && !jsonApiToOne.get().lazy()) {
			return Optional.of(SerializeType.ONLY_ID);
		}
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
	public Optional<LookupIncludeBehavior> getLookupIncludeBehavior(BeanAttributeInformation attributeDesc) {
		Optional<JsonApiRelation> jsonApiRelation = attributeDesc.getAnnotation(JsonApiRelation.class);
		if (jsonApiRelation.isPresent()) {
			return Optional.of(jsonApiRelation.get().lookUp());
		}

		Optional<JsonApiLookupIncludeAutomatically> jsonApiLookupIncludeAutomatically =
				attributeDesc.getAnnotation(JsonApiLookupIncludeAutomatically.class);
		if (jsonApiLookupIncludeAutomatically.isPresent()) {
			if (jsonApiLookupIncludeAutomatically.get().overwrite()) {
				return Optional.of(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
			} else {
				return Optional.of(LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL);
			}
		}

		return Optional.empty();
	}

	@Override
	public Optional<String> getOppositeName(BeanAttributeInformation attributeDesc) {
		Optional<JsonApiRelation> jsonApiRelation = attributeDesc.getAnnotation(JsonApiRelation.class);
		if (jsonApiRelation.isPresent()) {
			return Optional.ofNullable(StringUtils.emptyToNull(jsonApiRelation.get().opposite()));
		}

		Optional<JsonApiToMany> jsonApiToMany = attributeDesc.getAnnotation(JsonApiToMany.class);
		if (jsonApiToMany.isPresent()) {
			return Optional.ofNullable(StringUtils.emptyToNull(jsonApiToMany.get().opposite()));
		}

		Optional<JsonApiToOne> jsonApiToOne = attributeDesc.getAnnotation(JsonApiToOne.class);
		if (jsonApiToOne.isPresent()) {
			return Optional.ofNullable(StringUtils.emptyToNull(jsonApiToOne.get().opposite()));
		}
		return Optional.empty();
	}


	@Override
	public void init(ResourceInformationProviderContext context) {
		// nothing to do
	}

	@Override
	public Optional<Boolean> isIgnored(BeanAttributeInformation attributeDesc) {
		Field field = attributeDesc.getField();
		boolean isTransient = field != null && Modifier.isTransient(field.getModifiers());
		boolean relationshipIdField = attributeDesc.getAnnotation(JsonApiRelationId.class).isPresent();
		if (isTransient || relationshipIdField) {
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

		if (attributeDesc.getAnnotation(JsonApiToOne.class).isPresent()
				|| attributeDesc.getAnnotation(JsonApiToMany.class).isPresent()
				|| attributeDesc.getAnnotation(JsonApiRelation.class).isPresent()
		) {
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
