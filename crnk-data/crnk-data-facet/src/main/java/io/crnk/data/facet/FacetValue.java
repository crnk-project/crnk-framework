package io.crnk.data.facet;

import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.resource.annotations.JsonApiEmbeddable;

@JsonApiEmbeddable
public class FacetValue implements Comparable<FacetValue> {

    public static final String ATTR_LABEL = "label";

    private String label;

    private Object value;

    private FilterSpec filterSpec;

    private long count;

    @Override
    public String toString() {
        return FacetValue.class.getSimpleName() + "[label=" + label + ",value=" + value + ",count=" + count + "]";
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public FilterSpec getFilterSpec() {
        return filterSpec;
    }

    public void setFilterSpec(FilterSpec filterSpec) {
        this.filterSpec = filterSpec;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public int compareTo(FacetValue o) {
        return Long.compare(o.getCount(), count);
    }
}
