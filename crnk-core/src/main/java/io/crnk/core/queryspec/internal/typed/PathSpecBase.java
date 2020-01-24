package io.crnk.core.queryspec.internal.typed;

import io.crnk.core.queryspec.AbstractPathSpec;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.SortSpec;

import java.util.ArrayList;

public abstract class PathSpecBase extends PathSpec {

    protected AbstractPathSpec boundSpec;

    protected PathSpecBase(PathSpec pathSpec) {
        elements = pathSpec.getElements();
    }

    protected PathSpecBase(AbstractPathSpec boundSpec) {
        this(boundSpec.getPath());
        this.boundSpec = boundSpec;
    }

    protected abstract PathSpec bindSpec(AbstractPathSpec spec);

    public SortSpec sort(Direction dir) {
        if (boundSpec != null) {
            SortSpec sortSpec = (SortSpec) boundSpec;
            sortSpec.setDirection(dir);
            return sortSpec;
        }
        return new SortSpec(this, dir);
    }

    public FilterSpec filter(FilterOperator operator, Object value) {
        if (boundSpec != null) {
            FilterSpec filterSpec = (FilterSpec) boundSpec;
            filterSpec.setOperator(operator);
            filterSpec.setValue(value);
            return filterSpec;
        }
        return new FilterSpec(this, operator, value);
    }

    public PathSpec append(String fieldName) {
        ArrayList<String> copy = new ArrayList<>(elements);
        copy.add(fieldName);
        PathSpec updatedPathSpec = PathSpec.of(copy);
        if (boundSpec != null) {
            boundSpec.setPath(updatedPathSpec);
        }
        return updatedPathSpec;
    }
}
