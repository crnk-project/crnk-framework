package io.crnk.core.engine.http;

import java.util.Map;
import java.util.Set;

public interface HttpRequestContextBase {

    String getRequestHeader(String name);

    Map<String, Set<String>> getRequestParameters();

    String getPath();

    String getBaseUrl();

    byte[] getRequestBody();

    String getMethod();

    HttpResponse getResponse();

    void setResponse(HttpResponse response);
}
