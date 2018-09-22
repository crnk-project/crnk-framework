package io.crnk.core.engine.internal.http;

import java.util.List;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.internal.dispatcher.controller.Controller;
import io.crnk.core.module.Module;

class DocumentFilterChainImpl implements DocumentFilterChain {

	private final Module.ModuleContext moduleContext;
	protected int filterIndex = 0;

	protected Controller controller;

	public DocumentFilterChainImpl(Module.ModuleContext moduleContext, Controller controller) {
		this.moduleContext = moduleContext;
		this.controller = controller;
	}

	@Override
	public Response doFilter(DocumentFilterContext context) {
		List<DocumentFilter> filters = moduleContext.getDocumentFilters();
		if (filterIndex == filters.size()) {
			return controller.handle(context.getJsonPath(), context.getQueryAdapter(), context.getRequestBody());
		} else {
			DocumentFilter filter = filters.get(filterIndex);
			filterIndex++;
			return filter.filter(context, this);
		}
	}
}