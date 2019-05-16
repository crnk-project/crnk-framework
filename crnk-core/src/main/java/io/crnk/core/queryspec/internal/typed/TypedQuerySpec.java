package io.crnk.core.queryspec.internal.typed;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.IncludeFieldSpec;
import io.crnk.core.queryspec.IncludeRelationSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;

import java.util.ArrayList;
import java.util.List;

public class TypedQuerySpec<T, P extends ResourcePathSpec> extends QuerySpec {

    private final P basePath;

    protected TypedQuerySpec(Class<T> resourceClass, P basePath) {
        super(resourceClass);
        this.basePath = basePath;
    }

    public P sort() {
        PathSpec pathSpec = createNonEmptyPathSpec();
        SortSpec sortSpec = new SortSpec(pathSpec, Direction.ASC);
        pathSpec.getElements().remove(0);
        addSort(sortSpec);
        return (P) basePath.bindSpec(sortSpec);
    }

    public P filter() {
        PathSpec pathSpec = createNonEmptyPathSpec();
        FilterSpec filterSpec = new FilterSpec(pathSpec, FilterOperator.EQ, null);
        pathSpec.getElements().remove(0);
        addFilter(filterSpec);
        return (P) this.basePath.bindSpec(filterSpec);
    }

    public P include() {
        PathSpec pathSpec = createNonEmptyPathSpec();
        IncludeRelationSpec includeSpec = new IncludeRelationSpec(pathSpec);
        pathSpec.getElements().remove(0);
        getIncludedRelations().add(includeSpec);
        return (P) this.basePath.bindSpec(includeSpec);
    }

    public P field() {
        PathSpec pathSpec = createNonEmptyPathSpec();
        IncludeFieldSpec fieldSpec = new IncludeFieldSpec(pathSpec);
        pathSpec.getElements().remove(0);
        getIncludedFields().add(fieldSpec);
        return (P) this.basePath.bindSpec(fieldSpec);
    }

    private PathSpec createNonEmptyPathSpec() {
        List<String> list = new ArrayList<>();
        list.add("dummy");
        return PathSpec.of(list);
    }
}
