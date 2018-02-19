package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.resource.links.PagedLinksInformation;

public interface PagingSpec {

	/**
	 * Fills out the paging links
	 *
	 * @param linksInformation
	 * @param resources
	 * @param queryAdapter
	 * @param urlBuilder
	 */
	void buildPaging(PagedLinksInformation linksInformation, Iterable<?> resources,
					 QueryAdapter queryAdapter,
					 PagingSpecUrlBuilder urlBuilder);

	/**
	 * Determines whether Crnk needs to provide paging links via {@link #buildPaging(PagedLinksInformation, Iterable, QueryAdapter, PagingSpecUrlBuilder)}
	 *
	 * @return True in case of pagination is required otherwise False
	 */
	boolean isPagingRequired();
}
