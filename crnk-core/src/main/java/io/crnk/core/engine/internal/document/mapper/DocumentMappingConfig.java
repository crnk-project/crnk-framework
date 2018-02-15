package io.crnk.core.engine.internal.document.mapper;

import java.util.HashSet;
import java.util.Set;

import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

public class DocumentMappingConfig {

	private Set<String> fieldsWithEnforceIdSerialization = new HashSet<>();

	private RepositoryMethodParameterProvider parameterProvider;

	private ResourceMappingConfig resourceMapping = new ResourceMappingConfig();

	public Set<String> getFieldsWithEnforcedIdSerialization() {
		return fieldsWithEnforceIdSerialization;
	}

	public void setFieldsWithEnforcedIdSerialization(Set<String> fieldsWithEnforceIdSerialization) {
		this.fieldsWithEnforceIdSerialization = fieldsWithEnforceIdSerialization;
	}

	public RepositoryMethodParameterProvider getParameterProvider() {
		return parameterProvider;
	}

	public void setParameterProvider(RepositoryMethodParameterProvider parameterProvider) {
		this.parameterProvider = parameterProvider;
	}

	public ResourceMappingConfig getResourceMapping() {
		return resourceMapping;
	}

	public void setResourceMapping(ResourceMappingConfig resourceMapping) {
		this.resourceMapping = resourceMapping;
	}
}
