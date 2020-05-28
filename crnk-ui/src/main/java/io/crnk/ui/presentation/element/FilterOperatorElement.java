package io.crnk.ui.presentation.element;


import java.util.ArrayList;
import java.util.List;


public class FilterOperatorElement extends PresentationElement {

    private String defaultOperator;

    private List<String> available = new ArrayList<>();

    public String getDefaultOperator() {
        return defaultOperator;
    }

    public void setDefaultOperator(String defaultOperator) {
        this.defaultOperator = defaultOperator;
    }

    public List<String> getAvailable() {
        return available;
    }

    public void setAvailable(List<String> available) {
        this.available = available;
    }
}
