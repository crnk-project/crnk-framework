package io.crnk.core.engine.internal.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.internal.dispatcher.controller.*;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;

import java.util.HashSet;
import java.util.Set;

/**
 * This lookup gets all predefined Crnk controllers.
 */
public class DefaultControllerLookup implements ControllerLookup {

	private ResourceRegistry resourceRegistry;
	private TypeParser typeParser;
	private ObjectMapper objectMapper;
	private DocumentMapper documentMapper;
	private PropertiesProvider propertiesProvider;

	public DefaultControllerLookup(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider, TypeParser typeParser, ObjectMapper objectMapper, DocumentMapper documentMapper) {
		this.resourceRegistry = resourceRegistry;
		this.propertiesProvider = propertiesProvider;
		this.typeParser = typeParser;
		this.objectMapper = objectMapper;
		this.documentMapper = documentMapper;
	}

	@Override
	public Set<BaseController> getControllers() {
		Set<BaseController> controllers = new HashSet<>();
		controllers.add(new RelationshipsResourceDelete(resourceRegistry, typeParser));
		controllers.add(new RelationshipsResourcePatch(resourceRegistry, typeParser));
		controllers.add(new RelationshipsResourcePost(resourceRegistry, typeParser));
		controllers.add(new ResourceDelete(resourceRegistry, typeParser));
		controllers.add(new CollectionGet(resourceRegistry, objectMapper, typeParser, documentMapper));
		controllers.add(new FieldResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper));
		controllers.add(new RelationshipsResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper));
		controllers.add(new ResourceGet(resourceRegistry, objectMapper, typeParser, documentMapper));
		controllers.add(new FieldResourcePost(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper));
		controllers.add(new ResourcePatch(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper));
		controllers.add(new ResourcePost(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper));

		return controllers;
	}

}
