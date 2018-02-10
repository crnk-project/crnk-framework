package io.crnk.core.queryspec.paging;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;

public abstract class AbstractPagingSpec implements PagingSpec {

	protected String toUrl(QueryAdapter queryAdapter, RepositoryRequestSpec requestSpec,
						   ResourceRegistry resourceRegistry) {
		JsonApiUrlBuilder urlBuilder = new JsonApiUrlBuilder(resourceRegistry);
		Object relationshipSourceId = requestSpec.getId();
		ResourceField relationshipField = requestSpec.getRelationshipField();

		ResourceInformation rootInfo;
		if (relationshipField == null) {
			rootInfo = queryAdapter.getResourceInformation();
		} else {
			rootInfo = relationshipField.getParentResourceInformation();
		}
		return urlBuilder.buildUrl(rootInfo, relationshipSourceId, queryAdapter,
				relationshipField != null ? relationshipField.getJsonName() : null);
	}
}
