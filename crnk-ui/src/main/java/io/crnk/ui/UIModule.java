package io.crnk.ui;


import io.crnk.core.module.Module;
import io.crnk.ui.internal.UIHttpRequestProcessor;

public class UIModule implements Module {


	private final UIModuleConfig config;

	private UIModule(UIModuleConfig config) {
		this.config = config;
	}

	public static UIModule create(UIModuleConfig config) {
		return new UIModule(config);
	}


	public String getModuleName() {
		return "ui";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addHttpRequestProcessor(new UIHttpRequestProcessor(config));
	}
}
