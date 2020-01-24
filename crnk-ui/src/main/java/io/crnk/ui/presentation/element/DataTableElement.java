package io.crnk.ui.presentation.element;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class DataTableElement extends PresentationElement implements ContainerElement {

    private TableColumnsElement columns = new TableColumnsElement();

    private PaginationElement pagination = new PaginationElement();

    private DataTableEditElement edit = new DataTableEditElement();

    private boolean editable;

    @Override
    @JsonIgnore
    public List<PresentationElement> getChildren() {
        return new ArrayList<>(columns.getElements().values());
    }

    public TableColumnsElement getColumns() {
        return columns;
    }

    public void setColumns(TableColumnsElement columns) {
        this.columns = columns;
    }

    public PaginationElement getPagination() {
        return pagination;
    }

    public void setPagination(PaginationElement pagination) {
        this.pagination = pagination;
    }

    public DataTableEditElement getEdit() {
        return edit;
    }

    public void setEdit(DataTableEditElement edit) {
        this.edit = edit;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
