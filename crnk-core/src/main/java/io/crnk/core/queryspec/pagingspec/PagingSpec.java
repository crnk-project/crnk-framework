package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;

public interface PagingSpec {

	/**
	 * Fills out the paging links
	 *
	 * @param linksInformation {@link PagedLinksInformation} instance
	 * @param resources {@link ResourceList} of resources
	 * @param queryAdapter {@link QueryAdapter} instance
	 * @param urlBuilder {@link PagingSpecUrlBuilder} instance to provide a way to build a link
	 */
	void build(PagedLinksInformation linksInformation, ResourceList<?> resources,
			   QueryAdapter queryAdapter,
			   PagingSpecUrlBuilder urlBuilder);

	/**
	 * Determines whether Crnk needs to provide paging links via {@link #build(PagedLinksInformation, ResourceList, QueryAdapter, PagingSpecUrlBuilder)}
	 *
	 * @return True in case of pagination is required otherwise False
	 */
	boolean isRequired();
}
