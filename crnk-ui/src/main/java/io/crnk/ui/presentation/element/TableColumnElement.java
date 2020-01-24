package io.crnk.ui.presentation.element;

public class TableColumnElement extends WrapperElement {

    private String id;

    private String label;

    private boolean editable;

    private String width;

    private boolean sortable;

    private PresentationElement editComponent;

    private FilterElement filter;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public PresentationElement getEditComponent() {
        return editComponent;
    }

    public void setEditComponent(PresentationElement editComponent) {
        this.editComponent = editComponent;
    }

    public FilterElement getFilter() {
        return filter;
    }

    public void setFilter(FilterElement filter) {
        this.filter = filter;
    }
}
