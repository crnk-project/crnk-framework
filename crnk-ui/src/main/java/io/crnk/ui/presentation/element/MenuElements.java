package io.crnk.ui.presentation.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuElements extends PresentationElement {

    private Map<String, MenuElement> elements = new HashMap<>();

    private List<String> elementIds = new ArrayList<>();

    public void addElement(MenuElement menuElement) {
        elements.put(menuElement.getId(), menuElement);
        elementIds.add(menuElement.getId());
    }

    public Map<String, MenuElement> getElements() {
        return elements;
    }

    public void setElements(Map<String, MenuElement> elements) {
        this.elements = elements;
    }

    public List<String> getElementIds() {
        return elementIds;
    }

    public void setElementIds(List<String> elementIds) {
        this.elementIds = elementIds;
    }
}
