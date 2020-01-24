package io.crnk.ui.presentation.element;


public class FilterElement extends PresentationElement {

    private String path;

    private FilterOperatorElement operator = new FilterOperatorElement();

    private PresentationElement input;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FilterOperatorElement getOperator() {
        return operator;
    }

    public void setOperator(FilterOperatorElement operator) {
        this.operator = operator;
    }

    public PresentationElement getInput() {
        return input;
    }

    public void setInput(PresentationElement input) {
        this.input = input;
    }
}
