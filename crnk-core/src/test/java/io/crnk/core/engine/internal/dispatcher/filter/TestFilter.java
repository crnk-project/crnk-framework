package io.crnk.core.engine.internal.dispatcher.filter;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;

public class TestFilter implements DocumentFilter {

	@Override
	public Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain) {
		return chain.doFilter(filterRequestContext);
	}
}
