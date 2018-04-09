package io.crnk.core.engine.internal.dispatcher.controller;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.utils.Supplier;

public class ControllerContext {

	private final ModuleRegistry moduleRegistry;

	private Supplier<DocumentMapper> documentMapper;


	public ControllerContext(ModuleRegistry moduleRegistry, Supplier<DocumentMapper> documentMapper) {
		this.moduleRegistry = Objects.requireNonNull(moduleRegistry);
		this.documentMapper = documentMapper;
	}

	public ResourceFilterDirectory getResourceFilterDirectory() {
		return moduleRegistry.getContext().getResourceFilterDirectory();
	}

	public ResourceRegistry getResourceRegistry() {
		return moduleRegistry.getResourceRegistry();
	}

	public PropertiesProvider getPropertiesProvider() {
		return moduleRegistry.getPropertiesProvider();
	}

	public TypeParser getTypeParser() {
		return moduleRegistry.getTypeParser();
	}

	public ObjectMapper getObjectMapper() {
		return moduleRegistry.getContext().getObjectMapper();
	}

	public DocumentMapper getDocumentMapper() {
		return documentMapper.get();
	}

	public List<ResourceModificationFilter> getModificationFilters() {
		return moduleRegistry.getResourceModificationFilters();
	}

	public ResultFactory getResultFactory() {
		return moduleRegistry.getContext().getResultFactory();
	}

}
