package io.crnk.security.internal;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.utils.Supplier;
import io.crnk.security.DataRoomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class DataRoomMatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataRoomMatcher.class);


    private Supplier<DataRoomFilter> filter;

    public DataRoomMatcher(Supplier<DataRoomFilter> filter) {
        this.filter = filter;
    }

    public QuerySpec filter(QuerySpec querySpec, HttpMethod method) {
        return filter.get().filter(querySpec, method);
    }

    public boolean checkMatch(Object resource, HttpMethod method) {
        QuerySpec querySpec = filter(new QuerySpec(resource.getClass()), method);
        DefaultResourceList<Object> list = querySpec.apply(Collections.singleton(resource));
        return !list.isEmpty();
    }

    public void verifyMatch(Object resource, HttpMethod method) {
        boolean match = checkMatch(resource, method);
        if (!match) {
            LOGGER.error("dataroom prevented access to {} for {}", resource, method);
            throw new ForbiddenException("not allowed to access resource");
        }
    }
}
