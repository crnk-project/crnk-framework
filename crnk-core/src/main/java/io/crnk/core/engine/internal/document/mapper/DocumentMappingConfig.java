package io.crnk.core.engine.internal.document.mapper;

import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.util.HashSet;
import java.util.Set;

public class DocumentMappingConfig {

	private Set<String> fieldsWithEnforceIdSerialization = new HashSet<>();

	private RepositoryMethodParameterProvider parameterProvider;

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

	public RepositoryMethodParameterProvider getParameterProvider() {
		return parameterProvider;
	}

	public DocumentMappingConfig setParameterProvider(RepositoryMethodParameterProvider parameterProvider) {
		this.parameterProvider = parameterProvider;
		return this;
	}

	public ResourceMappingConfig getResourceMapping() {
		return resourceMapping;
	}

	public DocumentMappingConfig setResourceMapping(ResourceMappingConfig resourceMapping) {
		this.resourceMapping = resourceMapping;
		return this;
	}
}
