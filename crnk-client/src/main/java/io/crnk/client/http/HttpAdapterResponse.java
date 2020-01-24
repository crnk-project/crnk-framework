package io.crnk.client.http;

import java.io.IOException;
import java.util.Set;

public interface HttpAdapterResponse {

    boolean isSuccessful();

    String body() throws IOException;

    int code();

    String message();

    String getResponseHeader(String name);

    Set<String> getHeaderNames();

}
