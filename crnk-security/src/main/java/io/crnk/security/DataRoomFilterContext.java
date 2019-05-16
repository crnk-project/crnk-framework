package io.crnk.security;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.queryspec.QuerySpec;

public interface DataRoomFilterContext {

    QuerySpec getQuerySpec();

    HttpMethod getMethod();
}
