package io.crnk.ui.presentation.element;


public class ActionElement extends DisableableElement {

    private String label;

    private Object action;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getAction() {
        return action;
    }

    public void setAction(Object action) {
        this.action = action;
    }
}
