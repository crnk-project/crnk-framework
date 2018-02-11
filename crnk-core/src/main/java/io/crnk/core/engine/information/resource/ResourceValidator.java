package io.crnk.core.engine.information.resource;

import io.crnk.core.engine.document.Document;

/**
 * @deprecated
 */
@Deprecated
public interface ResourceValidator {

	void validate(Object entity, Document requestDocument);


}
