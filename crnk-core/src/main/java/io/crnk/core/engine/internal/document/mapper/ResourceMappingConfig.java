package io.crnk.core.engine.internal.document.mapper;

public class ResourceMappingConfig {

	private boolean serializeLinks = true;

	private boolean ignoreDefaults = false;

	private boolean serializeSelfRelationshipLinks = true;

	public boolean isIgnoreDefaults() {
		return ignoreDefaults;
	}

	public void setIgnoreDefaults(boolean ignoreDefaults) {
		this.ignoreDefaults = ignoreDefaults;
	}

	public boolean getSerializeLinks() {
		return serializeLinks;
	}

	public void setSerializeLinks(boolean serializeLinks) {
		this.serializeLinks = serializeLinks;
	}

	public boolean getSerializeSelfRelationshipLinks() {
		return serializeSelfRelationshipLinks;
	}

	public void setSerializeSelfRelationshipLinks(boolean serializeSelfRelationshipLinks) {
		this.serializeSelfRelationshipLinks = serializeSelfRelationshipLinks;
	}

	public ResourceMappingConfig clone() {
		ResourceMappingConfig config = new ResourceMappingConfig();
		config.setSerializeLinks(serializeLinks);
		config.setIgnoreDefaults(ignoreDefaults);
		config.setSerializeSelfRelationshipLinks(serializeSelfRelationshipLinks);
		return config;
	}
}
