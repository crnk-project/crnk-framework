package io.crnk.monitor.opentracing;

import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.module.Module;
import io.crnk.monitor.opentracing.internal.OpenTracingFilter;
import io.opentracing.Tracer;

public class OpenTracingServerModule implements Module {

	private final Tracer tracer;

	private boolean useSimpleTransactionNames = false;

	private boolean initialized;

	public OpenTracingServerModule(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public String getModuleName() {
		return "openTracing";
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.initialized = true;
		HttpRequestContextProvider requestContextProvider = context.getModuleRegistry().getHttpRequestContextProvider();
		context.addFilter(new OpenTracingFilter(requestContextProvider, tracer, useSimpleTransactionNames));
	}

	/**
	 * @param useSimpleTransactionNames to avoid the use of special characters in transaction names at the cost of readablity. Allows to
	 * workaround issues like https://github.com/elastic/kibana/issues/34866#issuecomment-493767648.
	 */
	public void setUseSimpleTransactionNames(boolean useSimpleTransactionNames) {
		PreconditionUtil.assertFalse("already initialized", initialized);
		this.useSimpleTransactionNames = useSimpleTransactionNames;
	}
}
