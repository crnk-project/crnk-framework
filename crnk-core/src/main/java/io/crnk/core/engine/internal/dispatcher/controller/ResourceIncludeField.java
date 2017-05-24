package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.ResourceRegistry;

/**
 * Created by zachncst on 10/14/15.
 */
public abstract class ResourceIncludeField extends BaseController {
	protected final ResourceRegistry resourceRegistry;
	protected final TypeParser typeParser;

	protected DocumentMapper documentMapper;

	public ResourceIncludeField(ResourceRegistry resourceRegistry, TypeParser typeParser, DocumentMapper documentMapper) {
		this.resourceRegistry = resourceRegistry;
		this.typeParser = typeParser;
		this.documentMapper = documentMapper;
	}
}
