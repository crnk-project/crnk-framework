package io.crnk.core.queryspec.pagingspec;

/**
 * Describes paging query specs
 */
public interface PagingSpec {


	default <T extends PagingSpec> T convert(Class<T> otherPagingSpecType) {
		throw new UnsupportedOperationException("cannot convert paging spec to " + otherPagingSpecType);
	}

	default PagingSpec clone() {
		return this; // TODO remove this default with crnk 3.0 to maintain backwardcompatibility in 2.x
	}

}
