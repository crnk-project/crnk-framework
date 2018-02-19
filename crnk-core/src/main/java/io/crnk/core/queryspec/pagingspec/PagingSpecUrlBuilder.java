package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;

public class PagingSpecUrlBuilder {

	private final ResourceRegistry resourceRegistry;

	private final RepositoryRequestSpec requestSpec;

	public PagingSpecUrlBuilder(final ResourceRegistry resourceRegistry, final RepositoryRequestSpec requestSpec) {
		this.resourceRegistry = resourceRegistry;
		this.requestSpec = requestSpec;
	}

	public String build(QueryAdapter queryAdapter) {
		JsonApiUrlBuilder urlBuilder = new JsonApiUrlBuilder(resourceRegistry);
		Object relationshipSourceId = requestSpec.getId();
		ResourceField relationshipField = requestSpec.getRelationshipField();

		ResourceInformation rootInfo;
		if (relationshipField == null) {
			rootInfo = queryAdapter.getResourceInformation();
		}
		else {
			rootInfo = relationshipField.getParentResourceInformation();
		}
		return urlBuilder.buildUrl(rootInfo, relationshipSourceId, queryAdapter,
				relationshipField != null ? relationshipField.getJsonName() : null);
	}
}
