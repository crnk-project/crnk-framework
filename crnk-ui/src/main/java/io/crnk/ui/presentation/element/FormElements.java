package io.crnk.ui.presentation.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormElements extends PresentationElement {

    private Map<String, FormElement> elements = new HashMap<>();

    private List<String> elementIds = new ArrayList<>();

    public Map<String, FormElement> getElements() {
        return elements;
    }

    public void setElements(Map<String, FormElement> elements) {
        this.elements = elements;
    }

    public List<String> getElementIds() {
        return elementIds;
    }

    public void setElementIds(List<String> elementIds) {
        this.elementIds = elementIds;
    }
}
