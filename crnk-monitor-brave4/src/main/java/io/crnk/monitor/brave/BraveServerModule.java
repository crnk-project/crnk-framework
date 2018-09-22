package io.crnk.monitor.brave;

import brave.Tracing;
import io.crnk.core.module.Module;
import io.crnk.monitor.brave.internal.BraveRepositoryFilter;


/**
 * Traces all repository accesses. Keep in mind that a single HTTP request
 * can trigger multiple repository accesses if the request contains an inclusion of relations.
 * Note that no HTTP calls itself are traced by this module. That is the responsibility of the
 * web container and Brave.
 */
public class BraveServerModule implements Module {

	private Tracing tracing;

	// protected for CDI
	protected BraveServerModule() {
	}

	private BraveServerModule(Tracing tracing) {
		this.tracing = tracing;
	}

	public static BraveServerModule create(Tracing tracing) {
		return new BraveServerModule(tracing);
	}

	@Override
	public String getModuleName() {
		return "brave-server";
	}

	@Override
	public void setupModule(ModuleContext context) {
		BraveRepositoryFilter filter = new BraveRepositoryFilter(tracing, context);
		context.addRepositoryFilter(filter);
	}

	public Tracing getTracing() {
		return tracing;
	}
}
