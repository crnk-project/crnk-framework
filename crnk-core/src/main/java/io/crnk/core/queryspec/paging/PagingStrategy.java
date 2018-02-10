package io.crnk.core.queryspec.paging;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.resource.links.PagedLinksInformation;

public interface PagingStrategy {

	void buildPaging(PagedLinksInformation linksInformation, Iterable<?> resources,
					 QueryAdapter queryAdapter, RepositoryRequestSpec requestSpec,
					 ResourceRegistry resourceRegistry);

	boolean requirePaging(PagingSpec pagingSpec);
}
