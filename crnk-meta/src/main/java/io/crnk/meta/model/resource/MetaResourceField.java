package io.crnk.meta.model.resource;

import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.meta.model.MetaAttribute;

/**
 * Field of a JSON API resource.
 */
@JsonApiResource(type = "meta/resourceField")
public class MetaResourceField extends MetaAttribute {

	private boolean meta;

	private boolean links;

	public boolean isMeta() {
		return meta;
	}

	public void setMeta(boolean meta) {
		this.meta = meta;
	}

	public boolean isLinks() {
		return links;
	}

	public void setLinks(boolean links) {
		this.links = links;
	}

}
