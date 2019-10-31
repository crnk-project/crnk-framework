package io.crnk.meta.model.resource;

import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.model.MetaAttribute;

/**
 * Field of a JSON API resource.
 */
@JsonApiResource(type = "meta/resourceField")
public class MetaResourceField extends MetaAttribute {

	private ResourceFieldType fieldType;

	public ResourceFieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(ResourceFieldType fieldType) {
		this.fieldType = fieldType;
	}
}
