package io.crnk.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.crnk.ui.presentation.PresentationService;
import io.crnk.ui.presentation.factory.PresentationElementFactory;

public class UIModuleConfig {

	private String path = "/browse/";

	private boolean browserEnabled = true;

	private boolean presentationModelEnabled = true;

	private Supplier<List<PresentationService>> services;

	private List<PresentationElementFactory> presentationElementFactories = new ArrayList<>();

	public boolean isBrowserEnabled() {
		return browserEnabled;
	}

	public void setBrowserEnabled(boolean browserEnabled) {
		this.browserEnabled = browserEnabled;
	}

	public boolean isPresentationModelEnabled() {
		return presentationModelEnabled;
	}

	public void setPresentationModelEnabled(boolean presentationModelEnabled) {
		this.presentationModelEnabled = presentationModelEnabled;
	}

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

	public void addPresentationElementFactory(PresentationElementFactory factory) {
		presentationElementFactories.add(factory);
	}

	public List<PresentationElementFactory> getPresentationElementFactories() {
		return presentationElementFactories;
	}

	public void setPresentationElementFactories(List<PresentationElementFactory> presentationElementFactories) {
		this.presentationElementFactories = presentationElementFactories;
	}
}
