package io.crnk.core.queryspec.pagingspec;

import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.ResourceList;

public interface PagingBehavior<T extends PagingSpec> {

	/**
	 * Provides serialization logic
	 *
	 * @param pagingSpec {@link PagingSpec} instance to deserialize
	 * @param resourceType Resource type
	 * @return map Filled out map object
	 */
	Map<String, Set<String>> serialize(T pagingSpec, String resourceType);

	/**
	 * Provides deserialization logic of a single iteration
	 *
	 * @param parameters Map of parameters to deserialize
	 * @return Filled out {@link PagingSpec} instance
	 * @throws {@link io.crnk.core.exception.ParametersDeserializationException} in case of deserialization error
	 */
	T deserialize(Map<String, Set<String>> parameters);

	/**
	 * Creates an empty instance of {@link PagingSpec}
	 * @return New {@link PagingSpec} instance
	 */
	T createEmptyPagingSpec();

	/**
	 * Creates an instance of {@link PagingSpec} with default values
	 * @return New {@link PagingSpec} instance
	 */
	T createDefaultPagingSpec();

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
	 * @param pagingSpec {@link PagingSpec} instance
	 * @return True in case of pagination is required otherwise False
	 */
	boolean isRequired(T pagingSpec);

	/**
	 * @param pagingSpecType
	 * @return true if the given paging spec can be provided by this behavior.
	 */
	default boolean supports(Class<? extends PagingSpec> pagingSpecType){
		T emptyPagingSpec = createEmptyPagingSpec();
		return pagingSpecType.isInstance(emptyPagingSpec);
	}
}
