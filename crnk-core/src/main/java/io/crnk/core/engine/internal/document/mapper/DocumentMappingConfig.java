package io.crnk.core.engine.internal.document.mapper;

import java.util.HashSet;
import java.util.Set;

public class DocumentMappingConfig {

	private Set<String> fieldsWithEnforceIdSerialization = new HashSet<>();

	private ResourceMappingConfig resourceMapping = new ResourceMappingConfig();

	public static DocumentMappingConfig create() {
		return new DocumentMappingConfig();
	}

	public Set<String> getFieldsWithEnforcedIdSerialization() {
		return fieldsWithEnforceIdSerialization;
	}

	public DocumentMappingConfig setFieldsWithEnforcedIdSerialization(Set<String> fieldsWithEnforceIdSerialization) {
		this.fieldsWithEnforceIdSerialization = fieldsWithEnforceIdSerialization;
		return this;
	}

	public ResourceMappingConfig getResourceMapping() {
		return resourceMapping;
	}

	public DocumentMappingConfig setResourceMapping(ResourceMappingConfig resourceMapping) {
		this.resourceMapping = resourceMapping;
		return this;
	}

	public DocumentMappingConfig clone() {
		DocumentMappingConfig config = new DocumentMappingConfig();
		config.setFieldsWithEnforcedIdSerialization(new HashSet<>(fieldsWithEnforceIdSerialization));
		config.setResourceMapping(resourceMapping.clone());
		return config;
	}
}
