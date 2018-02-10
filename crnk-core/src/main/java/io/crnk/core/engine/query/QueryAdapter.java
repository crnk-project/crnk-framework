package io.crnk.core.engine.query;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.paging.PagingSpec;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.params.IncludedFieldsParams;
import io.crnk.legacy.queryParams.params.IncludedRelationsParams;
import io.crnk.legacy.queryParams.params.TypedParams;

public interface QueryAdapter {

	TypedParams<IncludedRelationsParams> getIncludedRelations();

	TypedParams<IncludedFieldsParams> getIncludedFields();

	ResourceInformation getResourceInformation();

	PagingSpec getPagingSpec();

	void setPagingSpec(PagingSpec pagingSpec);

	/**
	 * @return duplicate of this instance
	 */
	QueryAdapter duplicate();

	/**
	 * The {@link QueryParams} instance for this query adapter if possible.
	 *
	 * @return may return null if the implementation does not support QueryParams
	 */
	@Deprecated
	QueryParams toQueryParams();

	/**
	 * The {@link QuerySpec} instance for this query adapter if possible.
	 *
	 * @return may return null if the implementation does not support QueryParams
	 */
	QuerySpec toQuerySpec();

	boolean getCompactMode();

}
