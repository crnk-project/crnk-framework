package io.crnk.core.queryspec.pagingspec;

/**
 * Describes paging query specs
 */
public interface PagingSpec {


	default PagingSpec clone() {
		return this; // TODO remove this default with crnk 3.0 to maintain backwardcompatibility in 2.x
	}

}
