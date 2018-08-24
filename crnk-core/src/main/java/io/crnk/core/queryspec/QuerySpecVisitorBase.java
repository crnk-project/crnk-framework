package io.crnk.core.queryspec;

import io.crnk.core.queryspec.pagingspec.PagingSpec;

public class QuerySpecVisitorBase implements QuerySpecVisitor {

	@Override
	public boolean visitStart(QuerySpec querySpec) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visitFilterStart(FilterSpec filterSpec) {
		// nothing to do
		return true;
	}

	@Override
	public void visitEnd(QuerySpec querySpec) {
		// nothing to do
	}

	@Override
	public boolean visitFilterEnd(FilterSpec filterSpec) {
		// nothing to do
		return true;
	}

	@Override
	public void visitPath(PathSpec pathSpec) {
		// nothing to do
	}

	@Override
	public boolean visitSort(SortSpec sortSpec) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visitField(IncludeFieldSpec fieldSpec) {
		// nothing to do
		return true;
	}

	@Override
	public boolean visitInclude(IncludeRelationSpec relationSpec) {
		// nothing to do
		return true;
	}

	@Override
	public void visitPaging(PagingSpec pagingSpec) {
		// nothing to do
	}
}
