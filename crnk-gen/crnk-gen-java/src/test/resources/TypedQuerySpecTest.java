package test;

import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;

import static test.UserPathSpec.*;

public class TypedQuerySpecTest {

    public QuerySpec createQuerySpec() {
        UserQuerySpec querySpec = new UserQuerySpec();
        querySpec.sort().loginId().desc();
        querySpec.filter().projects().id().filter(FilterOperator.EQ, 12);
        querySpec.field().loginId();
        querySpec.include().projects();
        return querySpec;
    }

    public FilterSpec createFilterSpec() {
        return userPathSpec.projects().id().filter(FilterOperator.EQ, 12);
    }

    public SortSpec createSortSpec() {
        return userPathSpec.projects().id().desc();
    }
}

