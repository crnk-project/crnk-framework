package io.crnk.core.queryspec.pagingspec;

/**
 * Allows to configure a paging behavior not have a default and max limit of number of returned results. Implemented by the
 * paging behaviors and accessed by
 * the Crnk setup mechanism.
 */
public interface LimitBoundedPagingBehavior<T extends PagingSpec> extends PagingBehavior<T> {

	Long getDefaultLimit();

	void setDefaultLimit(final Long defaultLimit);

	Long getMaxPageLimit();

	void setMaxPageLimit(final Long maxPageLimit);

}
