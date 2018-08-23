package io.crnk.core.queryspec;

import io.crnk.core.queryspec.pagingspec.PagingSpec;

/**
 * Allows to traverse a QuerySpec. Note that QuerySpec and FilterSpec can be nested. Because of this,
 * start/end methods are provided while other methods have just single visit method.
 */
public class QuerySpecVisitorBase implements QuerySpecVisitor {

	@Override
	public void visitStart(QuerySpec querySpec) {
		// nothing to do
	}

	@Override
	public void visitFilterStart(FilterSpec filterSpec) {
		// nothing to do
	}

	@Override
	public void visitEnd(QuerySpec querySpec) {
		// nothing to do
	}

	@Override
	public void visitFilterEnd(FilterSpec filterSpec) {
		// nothing to do
	}

	@Override
	public void visitPath(PathSpec pathSpec) {
		// nothing to do
	}

	@Override
	public void visitSort(SortSpec sortSpec) {
		// nothing to do
	}

	@Override
	public void visitField(IncludeFieldSpec fieldSpec) {
		// nothing to do
	}

	@Override
	public void visitInclude(IncludeRelationSpec relationSpec) {
		// nothing to do
	}

	@Override
	public void visitPaging(PagingSpec pagingSpec) {
		// nothing to do
	}
}
