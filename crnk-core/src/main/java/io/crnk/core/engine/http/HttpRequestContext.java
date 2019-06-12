package io.crnk.core.engine.http;

import io.crnk.core.engine.query.QueryContext;

public interface HttpRequestContext extends HttpRequestContextBase {

    boolean accepts(String contentType);

    boolean acceptsAny();

    <T> T unwrap(Class<T> type);

    Object getRequestAttribute(String name);

    void setRequestAttribute(String name, Object value);

    QueryContext getQueryContext();
}
