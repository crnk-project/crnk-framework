package io.crnk.ui;


import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtension;
import io.crnk.ui.internal.UIHttpRequestProcessor;

import java.lang.reflect.Method;

public class UIModule implements Module {


	private final UIModuleConfig config;

	// protected for CDI
	protected UIModule() {
		config = null;
	}

	protected UIModule(UIModuleConfig config) {
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
		setupHomeExtension(context);
	}

	public UIModuleConfig getConfig() {
		return config;
	}

	private void setupHomeExtension(ModuleContext context) {
		if (ClassUtils.existsClass("io.crnk.home.HomeModuleExtension")) {
			try {
				Class clazz = Class.forName("io.crnk.ui.internal.UiHomeModuleExtensionFactory");
				Method method = clazz.getMethod("create", UIModuleConfig.class);
				ModuleExtension homeExtension = (ModuleExtension) method.invoke(clazz, config);
				context.addExtension(homeExtension);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
