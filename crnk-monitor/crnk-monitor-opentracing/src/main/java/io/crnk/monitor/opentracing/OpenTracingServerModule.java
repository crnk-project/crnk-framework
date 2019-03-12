package io.crnk.monitor.opentracing;

import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.module.Module;
import io.crnk.monitor.opentracing.internal.OpenTracingFilter;
import io.opentracing.Tracer;

public class OpenTracingServerModule implements Module {

	private final Tracer tracer;

	public OpenTracingServerModule(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public String getModuleName() {
		return "openTracing";
	}

	@Override
	public void setupModule(ModuleContext context) {
		HttpRequestContextProvider requestContextProvider = context.getModuleRegistry().getHttpRequestContextProvider();
		context.addFilter(new OpenTracingFilter(requestContextProvider, tracer));
	}
}
