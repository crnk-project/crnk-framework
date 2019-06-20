package io.crnk.ui.presentation.element;


public class FormElement extends SingularValueElement {

	private PresentationElement component;

	private String label;

	private boolean editable;

	public PresentationElement getComponent() {
		return component;
	}

	public void setComponent(PresentationElement component) {
		this.component = component;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
}
