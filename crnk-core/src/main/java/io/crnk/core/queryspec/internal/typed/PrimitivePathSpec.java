package io.crnk.core.queryspec.internal.typed;

import io.crnk.core.queryspec.AbstractPathSpec;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.SortSpec;

public class PrimitivePathSpec<T> extends PathSpecBase {

    public PrimitivePathSpec(PathSpec pathSpec) {
        super(pathSpec);
    }

    public PrimitivePathSpec(AbstractPathSpec boundSpec) {
        super(boundSpec);
    }

    @Override
    protected PrimitivePathSpec<T> bindSpec(AbstractPathSpec spec) {
        return new PrimitivePathSpec<>(spec);
    }

    public SortSpec desc() {
        return sort(Direction.DESC);
    }

    public SortSpec asc() {
        return sort(Direction.ASC);
    }

    public FilterSpec eq(T value) {
        return filter(FilterOperator.EQ, value);
    }

    public FilterSpec neq(T value) {
        return filter(FilterOperator.NEQ, value);
    }

    public FilterSpec like(T value) {
        return filter(FilterOperator.LIKE, value);
    }

    public FilterSpec gt(T value) {
        return filter(FilterOperator.GT, value);
    }

    public FilterSpec ge(T value) {
        return filter(FilterOperator.GE, value);
    }

    public FilterSpec lt(T value) {
        return filter(FilterOperator.LT, value);
    }

    public FilterSpec le(T value) {
        return filter(FilterOperator.LE, value);
    }
}
