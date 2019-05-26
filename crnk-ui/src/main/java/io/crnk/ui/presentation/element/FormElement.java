package io.crnk.ui.presentation.element;


public class FormElement extends PresentationElement {

    private PresentationElement component;

    private String label;

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
}
