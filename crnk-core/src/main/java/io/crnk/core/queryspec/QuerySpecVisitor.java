package io.crnk.core.queryspec;

import io.crnk.core.queryspec.pagingspec.PagingSpec;

public interface QuerySpecVisitor {

	void visitStart(QuerySpec querySpec);

	void visitEnd(QuerySpec querySpec);

	void visitFilterStart(FilterSpec filterSpec);

	void visitFilterEnd(FilterSpec filterSpec);

	void visitPath(PathSpec pathSpec);

	void visitSort(SortSpec sortSpec);

	void visitField(IncludeFieldSpec fieldSpec);

	void visitInclude(IncludeRelationSpec relationSpec);

	void visitPaging(PagingSpec pagingSpec);

}
