package io.crnk.core.engine.internal.document.mapper;

import io.crnk.core.engine.query.QueryContext;

public class ResourceMappingConfig {

	private boolean serializeLinks = true;

	public boolean getSerializeLinks() {
		return serializeLinks;
	}

	public void setSerializeLinks(boolean serializeLinks) {
		this.serializeLinks = serializeLinks;
	}
}
