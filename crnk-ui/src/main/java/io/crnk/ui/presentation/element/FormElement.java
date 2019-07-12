package io.crnk.ui.presentation.element;


public class FormElement extends WrapperElement {

	private String label;

	private boolean editable;

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
