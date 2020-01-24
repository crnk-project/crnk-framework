package io.crnk.ui.presentation.element;

import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.SortSpec;

import java.util.ArrayList;
import java.util.List;

public class QueryElement extends PresentationElement {

    private String resourceType;

    private long offset;

    private Long limit;

    private List<FilterSpec> filtering = new ArrayList<>();

    private List<SortSpec> sorting = new ArrayList<>();

    private List<PathSpec> inclusions = new ArrayList<>();

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public List<FilterSpec> getFiltering() {
        return filtering;
    }

    public void setFiltering(List<FilterSpec> filtering) {
        this.filtering = filtering;
    }

    public List<SortSpec> getSorting() {
        return sorting;
    }

    public void setSorting(List<SortSpec> sorting) {
        this.sorting = sorting;
    }

    public List<PathSpec> getInclusions() {
        return inclusions;
    }

    public void setInclusions(List<PathSpec> inclusions) {
        this.inclusions = inclusions;
    }
}
