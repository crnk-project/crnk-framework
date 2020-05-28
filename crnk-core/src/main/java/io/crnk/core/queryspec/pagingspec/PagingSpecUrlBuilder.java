package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.mapper.UrlBuilder;

public class PagingSpecUrlBuilder {

	private final ModuleRegistry moduleRegistry;

	private final RepositoryRequestSpec requestSpec;


	public PagingSpecUrlBuilder(final ModuleRegistry moduleRegistry, final RepositoryRequestSpec requestSpec) {
		this.moduleRegistry = moduleRegistry;
		this.requestSpec = requestSpec;
	}

	public String build(QueryAdapter queryAdapter) {
		QueryContext queryContext = queryAdapter.getQueryContext();

		UrlBuilder urlBuilder = moduleRegistry.getUrlBuilder();

		Object relationshipSourceId = requestSpec.getId();
		ResourceField relationshipField = requestSpec.getRelationshipField();

		ResourceInformation rootInfo;
		if (relationshipField == null) {
			rootInfo = queryAdapter.getResourceInformation();
		} else {
			rootInfo = relationshipField.getResourceInformation();
		}
		return urlBuilder.buildUrl(queryContext, rootInfo, relationshipSourceId, queryAdapter.toQuerySpec(),
				relationshipField != null ? relationshipField.getJsonName() : null, queryAdapter.isSelfLink());
	}
}
