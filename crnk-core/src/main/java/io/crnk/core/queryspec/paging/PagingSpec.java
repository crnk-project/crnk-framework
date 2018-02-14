package io.crnk.core.queryspec.paging;

import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.resource.links.PagedLinksInformation;

import java.util.function.Function;

public interface PagingSpec {

	/**
	 * Fills out the paging links
	 *
	 * @param linksInformation
	 * @param resources
	 * @param queryAdapter
	 * @param urlFn
	 */
	void buildPaging(PagedLinksInformation linksInformation, Iterable<?> resources,
					 QueryAdapter queryAdapter,
					 Function<QueryAdapter, String> urlFn);

	/**
	 * Determines whether Crnk needs to provide paging links via {@link #buildPaging(PagedLinksInformation, Iterable, QueryAdapter, Function)}
	 *
	 * @return True in case of pagination is required otherwise False
	 */
	boolean isPagingRequired();
}
