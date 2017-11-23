package io.crnk.core.engine.internal.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.internal.dispatcher.controller.*;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This lookup gets all predefined Crnk controllers.
 */
public class DefaultControllerLookup implements ControllerLookup {

	private final List<ResourceModificationFilter> modificationFilters;
	private final ResourceRegistry resourceRegistry;
	private final TypeParser typeParser;
	private final ObjectMapper objectMapper;
	private final DocumentMapper documentMapper;
	private final PropertiesProvider propertiesProvider;

	public DefaultControllerLookup(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider, TypeParser typeParser, ObjectMapper objectMapper, DocumentMapper documentMapper, List<ResourceModificationFilter> modificationFilters) {
		this.resourceRegistry = resourceRegistry;
		this.propertiesProvider = propertiesProvider;
		this.typeParser = typeParser;
		this.objectMapper = objectMapper;
		this.documentMapper = documentMapper;
		this.modificationFilters = modificationFilters;
	}

	@Override
	public Set<BaseController> getControllers() {
		Set<BaseController> controllers = new HashSet<>();
		controllers.add(new RelationshipsResourceDelete(resourceRegistry, typeParser, modificationFilters));
		controllers.add(new RelationshipsResourcePatch(resourceRegistry, typeParser, modificationFilters));
		controllers.add(new RelationshipsResourcePost(resourceRegistry, typeParser, modificationFilters));
		controllers.add(new ResourceDelete(resourceRegistry, modificationFilters));
		controllers.add(new CollectionGet(resourceRegistry, typeParser, documentMapper));
		controllers.add(new FieldResourceGet(resourceRegistry, typeParser, documentMapper));
		controllers.add(new RelationshipsResourceGet(resourceRegistry, typeParser, documentMapper));
		controllers.add(new ResourceGet(resourceRegistry, typeParser, documentMapper));
		controllers.add(new FieldResourcePost(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper, modificationFilters));
		controllers.add(new ResourcePatch(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper, modificationFilters));
		controllers.add(new ResourcePost(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper, modificationFilters));
		return controllers;
	}

}
