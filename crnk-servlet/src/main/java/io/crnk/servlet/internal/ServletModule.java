package io.crnk.servlet.internal;

import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.module.Module;

public class ServletModule implements Module {


	private HttpRequestContextProvider contextProvider;

	public ServletModule(HttpRequestContextProvider contextProvider) {
		this.contextProvider = contextProvider;
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addSecurityProvider(new ServletSecurityProvider(contextProvider));
	}

	@Override
	public String getModuleName() {
		return "servlet";
	}
}
