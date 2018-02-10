package io.crnk.core.queryspec.paging;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.resource.links.PagedLinksInformation;

public interface PagingSpec {

	/**
	 * Fills out the paging links
	 *
	 * @param linksInformation
	 * @param resources
	 * @param queryAdapter
	 * @param requestSpec
	 * @param resourceRegistry
	 */
	void buildPaging(PagedLinksInformation linksInformation, Iterable<?> resources,
					 QueryAdapter queryAdapter, RepositoryRequestSpec requestSpec,
					 ResourceRegistry resourceRegistry);

	/**
	 * Determines whether Crnk needs to provide paging links via {@link #buildPaging(PagedLinksInformation, Iterable, QueryAdapter, RepositoryRequestSpec, ResourceRegistry)}
	 *
	 * @return True in case of pagination is required otherwise False
	 */
	boolean isPagingRequired();
}
