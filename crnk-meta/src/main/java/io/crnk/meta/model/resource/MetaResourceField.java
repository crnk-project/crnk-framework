package io.crnk.meta.model.resource;

import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.VersionRange;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.model.MetaAttribute;

/**
 * Field of a JSON API resource.
 */
@JsonApiResource(type = "metaResourceField", resourcePath = "meta/resourceField")
public class MetaResourceField extends MetaAttribute {

	private ResourceFieldType fieldType;

	private VersionRange versionRange;

	public ResourceFieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(ResourceFieldType fieldType) {
		this.fieldType = fieldType;
	}

	public VersionRange getVersionRange() {
		return versionRange;
	}

	public void setVersionRange(VersionRange versionRange) {
		this.versionRange = versionRange;
	}
}
