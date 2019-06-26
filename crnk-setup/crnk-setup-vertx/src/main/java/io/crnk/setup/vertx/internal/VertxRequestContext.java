package io.crnk.setup.vertx.internal;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.http.HttpServerRequest;

public class VertxRequestContext implements HttpRequestContextBase {

    private final HttpServerRequest serverRequest;

    private byte[] requestBody;

    private HttpResponse response = new HttpResponse();

    private String pathPrefix;

    public VertxRequestContext(final HttpServerRequest serverRequest, String pathPrefix) {
        this.pathPrefix = pathPrefix;
        this.serverRequest = serverRequest;
    }

    public void setRequestBody(byte[] requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public String getRequestHeader(String name) {
        return serverRequest.getHeader(name);
    }

    @Override
    public Map<String, Set<String>> getRequestParameters() {
        MultiMap params = serverRequest.params();

        Map<String, Set<String>> map = new HashMap<>();
        for (String name : params.names()) {
            HashSet<String> set = new HashSet<>(params.getAll(name));
            map.put(name, set);
        }
        return map;
    }

    @Override
    public String getPath() {
        String path = serverRequest.path();
        if (pathPrefix != null && path.startsWith(pathPrefix)) {
            path = path.substring(pathPrefix.length());
        }
        return path.isEmpty() ? "/" : path;
    }

    @Override
    public String getBaseUrl() {
        String uri = UrlUtils.removeTrailingSlash(serverRequest.absoluteURI());
        int paramSep = uri.indexOf("?");
        if (paramSep != -1) {
            uri = uri.substring(0, paramSep);
        }
        String path = UrlUtils.removeTrailingSlash(getPath());
        PreconditionUtil.verify(uri.endsWith(path), "expected %s to end with %s", uri, path);
        return UrlUtils.removeTrailingSlash(uri.substring(0, uri.length() - path.length()));

    }

    @Override
    public byte[] getRequestBody() {
        return requestBody;
    }

    @Override
    public String getMethod() {
        return serverRequest.rawMethod();
    }

	@Override
	public URI getRequestUri() {
		return URI.create(serverRequest.uri());
	}


	@Override
    public HttpResponse getResponse() {
        return response;
    }

    @Override
    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public HttpServerRequest getServerRequest() {
        return serverRequest;
    }
}
