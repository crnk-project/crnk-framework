package io.crnk.ui.presentation.element;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionContainerElement extends PresentationElement {

    private List<String> actionIds = new ArrayList<>();

    private Map<String, ActionElement> actions = new HashMap<>();

    public List<String> getActionIds() {
        return actionIds;
    }

    public void setActionIds(List<String> actionIds) {
        this.actionIds = actionIds;
    }

    public Map<String, ActionElement> getActions() {
        return actions;
    }

    public void setActions(Map<String, ActionElement> actions) {
        this.actions = actions;
    }
}
