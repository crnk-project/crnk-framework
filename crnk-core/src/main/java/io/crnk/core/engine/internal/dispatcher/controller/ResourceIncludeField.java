package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.exception.RequestBodyNotFoundException;
import io.crnk.core.exception.ResourceFieldNotFoundException;

public abstract class ResourceIncludeField extends BaseController {

	protected final ResourceRegistry resourceRegistry;

	protected final TypeParser typeParser;

	protected DocumentMapper documentMapper;

	public ResourceIncludeField(ResourceRegistry resourceRegistry, TypeParser typeParser, DocumentMapper documentMapper) {
		this.resourceRegistry = resourceRegistry;
		this.typeParser = typeParser;
		this.documentMapper = documentMapper;
	}

	protected static void verifyFieldNotNull(ResourceField field, String elementName) {
		if (field == null) {
			throw new ResourceFieldNotFoundException(elementName);
		}
	}

	protected void assertRequestDocument(Document requestDocument, HttpMethod method, String resourceType) {
		if (requestDocument == null) {
			throw new RequestBodyNotFoundException(method, resourceType);
		}
	}

	protected RegistryEntry getRegistryEntry(String resourceType) {
		RegistryEntry registryEntry = resourceRegistry.getEntry(resourceType);
		if (registryEntry == null) {
			throw new RepositoryNotFoundException(resourceType);
		}
		return registryEntry;
	}

}
