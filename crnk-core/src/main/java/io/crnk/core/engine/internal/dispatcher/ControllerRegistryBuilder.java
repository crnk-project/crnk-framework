package io.crnk.core.engine.internal.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.internal.dispatcher.controller.BaseController;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.legacy.internal.DefaultExceptionMapperLookup;

import java.util.LinkedList;
import java.util.List;

/**
 * A builder class which holds all of the Crnk controllers, which must be placed in
 * {@link io.crnk.core.engine.http.internal.dispatcher.controller} package.
 */
public class ControllerRegistryBuilder {

	private final ResourceRegistry resourceRegistry;
	private final TypeParser typeParser;
	private final ObjectMapper objectMapper;
	private final DocumentMapper documentMapper;
	private final PropertiesProvider propertiesProvider;
	private final List<ResourceModificationFilter> modificationFilters;

	public ControllerRegistryBuilder(@SuppressWarnings("SameParameterValue") ResourceRegistry resourceRegistry, @SuppressWarnings("SameParameterValue") TypeParser typeParser,
									 @SuppressWarnings("SameParameterValue") ObjectMapper objectMapper, PropertiesProvider propertiesProvider,
									 ResourceFilterDirectory resourceFilterDirectory, List<ResourceModificationFilter> modificationFilters) {
		this.resourceRegistry = resourceRegistry;
		this.typeParser = typeParser;
		this.propertiesProvider = propertiesProvider;
		this.objectMapper = objectMapper;
		this.documentMapper = new DocumentMapper(resourceRegistry, objectMapper, propertiesProvider, resourceFilterDirectory);
		this.modificationFilters = modificationFilters;
	}

	/**
	 * Uses the given {@link ControllerLookup} to get all controllers.
	 *
	 * @param lookup an instance of a lookup class to get the controllers
	 * @return an instance of {@link ControllerRegistry} with initialized controllers
	 */
	private static ControllerRegistry build(ControllerLookup lookup) {
		List<BaseController> controllers = new LinkedList<>();
		controllers.addAll(lookup.getControllers());
		return new ControllerRegistry(controllers);
	}

	/**
	 * Uses the {@link DefaultExceptionMapperLookup} to collect all controllers.
	 *
	 * @return an instance of {@link ControllerRegistry} with initialized controllers
	 */
	public ControllerRegistry build() {
		return build(new DefaultControllerLookup(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper, modificationFilters));
	}

	public DocumentMapper getDocumentMapper() {
		return documentMapper;
	}
}
