package io.crnk.ui.presentation.element;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class EditorElement extends PresentationElement {

    private ViewHeaderElement header = new ViewHeaderElement();

    private QueryElement baseQuery = new QueryElement();

    private ActionContainerElement actions = new ActionContainerElement();

    private FormContainerElement form = new FormContainerElement();

    private ObjectNode newResourceTemplate;

    public ViewHeaderElement getHeader() {
        return header;
    }

    public void setHeader(ViewHeaderElement header) {
        this.header = header;
    }

    public QueryElement getBaseQuery() {
        return baseQuery;
    }

    public void setBaseQuery(QueryElement baseQuery) {
        this.baseQuery = baseQuery;
    }

    public ActionContainerElement getActions() {
        return actions;
    }

    public void setActions(ActionContainerElement actions) {
        this.actions = actions;
    }

    public FormContainerElement getForm() {
        return form;
    }

    public void setForm(FormContainerElement form) {
        this.form = form;
    }

    public ObjectNode getNewResourceTemplate() {
        return newResourceTemplate;
    }

    public void setNewResourceTemplate(ObjectNode newResourceTemplate) {
        this.newResourceTemplate = newResourceTemplate;
    }
}
