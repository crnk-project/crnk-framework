package io.crnk.core.engine.http;

import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.query.QueryContext;

public interface HttpRequestContext extends HttpRequestContextBase {

    static HttpRequestContext create(HttpRequestContextBase baseImpl) {
        return new HttpRequestContextBaseAdapter(baseImpl);
    }

    boolean accepts(String contentType);

    boolean acceptsAny();

    <T> T unwrap(Class<T> type);

    Object getRequestAttribute(String name);

    void setRequestAttribute(String name, Object value);

    QueryContext getQueryContext();

    boolean hasResponse();
}
