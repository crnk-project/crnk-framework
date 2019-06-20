package io.crnk.ui.presentation;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;

import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaType;
import io.crnk.ui.presentation.element.PresentationElement;

public class PresentationEnvironment {

	private MetaElement element;

	private MetaType type;

	private ArrayDeque<MetaAttribute> attributePath;

	private boolean editable;

	private List<PresentationType> acceptedTypes = Collections.emptyList();

	private PresentationManager manager;

	private PresentationService service;

	public PresentationElement createElement(PresentationEnvironment env) {
		return manager.createElement(env);
	}

	public MetaElement getElement() {
		return element;
	}

	public void setElement(MetaElement element) {
		this.element = element;
	}

	public MetaType getType() {
		return type;
	}

	public void setType(MetaType type) {
		this.type = type;
	}

	public ArrayDeque<MetaAttribute> getAttributePath() {
		return attributePath;
	}

	public PresentationEnvironment setAttributePath(ArrayDeque<MetaAttribute> attributePath) {
		this.attributePath = attributePath;
		return this;
	}

	public boolean isEditable() {
		return editable;
	}

	public PresentationEnvironment setEditable(boolean editable) {
		this.editable = editable;
		return this;
	}

	public List<PresentationType> getAcceptedTypes() {
		return acceptedTypes;
	}

	public PresentationEnvironment setAcceptedTypes(List<PresentationType> acceptedTypes) {
		this.acceptedTypes = acceptedTypes;
		return this;
	}

	public PresentationManager getManager() {
		return manager;
	}

	public PresentationEnvironment setManager(PresentationManager manager) {
		this.manager = manager;
		return this;
	}

	public PresentationService getService() {
		return service;
	}

	public PresentationEnvironment setService(PresentationService service) {
		this.service = service;
		return this;
	}

	public PresentationEnvironment clone() {
		PresentationEnvironment duplicate = new PresentationEnvironment();
		duplicate.setElement(element);
		duplicate.setType(type);
		duplicate.setAttributePath(attributePath);
		duplicate.setEditable(editable);
		duplicate.setAcceptedTypes(acceptedTypes);
		duplicate.setManager(manager);
		duplicate.setService(service);
		return duplicate;
	}
}

