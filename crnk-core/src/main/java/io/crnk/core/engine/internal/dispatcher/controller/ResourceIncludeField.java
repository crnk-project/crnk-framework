package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.exception.RequestBodyNotFoundException;
import io.crnk.core.exception.ResourceFieldNotFoundException;

public abstract class ResourceIncludeField extends BaseController {

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
}
