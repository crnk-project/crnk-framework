package io.crnk.core.queryspec;

import io.crnk.core.queryspec.pagingspec.PagingSpec;

/**
 * Allows to traverse a QuerySpec. Note that QuerySpec and FilterSpec can be nested. Because of this,
 * start/end methods are provided while other methods have just single visit method.
 */
public interface QuerySpecVisitor {

	/**
	 * @return true if nested objects like sorting, filtering, etc. should be visited.
	 */
	boolean visitStart(QuerySpec querySpec);

	void visitEnd(QuerySpec querySpec);

	/**
	 * @return true if PathSpec and nested FilterSpec should be visited.
	 */
	boolean visitFilterStart(FilterSpec filterSpec);

	/**
	 * @return true if PathSpec should be visited.
	 */
	boolean visitFilterEnd(FilterSpec filterSpec);

	void visitPath(PathSpec pathSpec);

	/**
	 * @return true if PathSpec should be visited.
	 */
	boolean visitSort(SortSpec sortSpec);

	/**
	 * @return true if PathSpec should be visited.
	 */
	boolean visitField(IncludeFieldSpec fieldSpec);

	/**
	 * @return true if PathSpec should be visited.
	 */
	boolean visitInclude(IncludeRelationSpec relationSpec);

	void visitPaging(PagingSpec pagingSpec);

}
