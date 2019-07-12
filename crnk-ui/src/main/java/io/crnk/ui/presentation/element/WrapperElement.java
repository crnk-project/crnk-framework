package io.crnk.ui.presentation.element;

public class WrapperElement extends SingularValueElement {

	private PresentationElement component;

	public PresentationElement getComponent() {
		return component;
	}

	public void setComponent(PresentationElement component) {
		this.component = component;
	}
}
