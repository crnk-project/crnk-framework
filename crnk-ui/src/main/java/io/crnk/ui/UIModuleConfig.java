package io.crnk.ui;

import java.util.List;
import java.util.function.Supplier;

import io.crnk.ui.presentation.PresentationService;

public class UIModuleConfig {

	private String path = "/browse/";

	private Supplier<List<PresentationService>> services;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Supplier<List<PresentationService>> getServices() {
		return services;
	}

	public void setServices(Supplier<List<PresentationService>> services) {
		this.services = services;
	}
}
