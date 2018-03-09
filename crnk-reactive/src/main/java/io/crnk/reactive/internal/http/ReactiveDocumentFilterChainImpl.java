package io.crnk.reactive.internal.http;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.internal.dispatcher.controller.Controller;
import io.crnk.core.module.Module;
import io.crnk.reactive.engine.document.ReactiveDocumentFilterChain;
import reactor.core.publisher.Mono;

class ReactiveDocumentFilterChainImpl implements ReactiveDocumentFilterChain {

	private final Module.ModuleContext moduleContext;
	protected int filterIndex = 0;

	protected Controller controller;

	public ReactiveDocumentFilterChainImpl(Module.ModuleContext moduleContext, Controller controller) {
		this.moduleContext = moduleContext;
		this.controller = controller;
	}

	@Override
	public Mono<Response> doFilter(DocumentFilterContext context) {
		return null;
		// FIXME
		/*
		List<ReactiveDocumentFilter> filters = moduleContext.getInstancesByType(ReactiveDocumentFilter.class);
		if (filterIndex == filters.size()) {
			Result<Response> response = controller.handleAsync(context.getJsonPath(), context.getQueryAdapter(), context.getParameterProvider(),
					context.getRequestBody());
			return MonoAdapter.adapt(response);
		} else {
			ReactiveDocumentFilter filter = filters.get(filterIndex);
			filterIndex++;
			return filter.filter(context, this);
		}
		*/
	}
}