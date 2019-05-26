package io.crnk.ui.presentation.element;


public class FormContainerElement extends PresentationElement {

    private FormElements elements = new FormElements();

    private String maxWidth;

    public FormElements getElements() {
        return elements;
    }

    public void setElements(FormElements elements) {
        this.elements = elements;
    }

    public String getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(String maxWidth) {
        this.maxWidth = maxWidth;
    }
}
