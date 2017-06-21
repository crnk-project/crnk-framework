package io.crnk.meta.model.resource;

import io.crnk.core.resource.annotations.JsonApiResource;

import java.util.List;

/**
 * Base class for resource classes. Such objects have the same
 * structure (attributes, relationships, etc.) as resources, but
 * are not resources by themselves. They do not carry a resourceType.
 */
@JsonApiResource("meta/resourceBase")
public class MetaResourceBase extends MetaJsonObject {

	@Override
	public List<MetaResourceField> getDeclaredAttributes() {
		return (List<MetaResourceField>) super.getDeclaredAttributes();
	}
}
