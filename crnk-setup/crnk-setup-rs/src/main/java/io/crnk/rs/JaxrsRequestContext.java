package io.crnk.rs;

import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.utils.Nullable;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JaxrsRequestContext implements HttpRequestContextBase {

    private final CrnkFeature feature;

    private ContainerRequestContext requestContext;

    private String path;

    private Map<String, Set<String>> parameters;

    private Nullable<byte[]> requestBody = Nullable.empty();

    private HttpResponse response = new HttpResponse();

    JaxrsRequestContext(ContainerRequestContext requestContext, CrnkFeature feature) {
        this.feature = feature;
        this.requestContext = requestContext;

        UriInfo uriInfo = requestContext.getUriInfo();
        this.path = buildPath(uriInfo);
        this.parameters = getParameters(uriInfo);
    }

    @Override
    public HttpResponse getResponse() {
        return response;
    }

    @Override
    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    @Override
    public String getRequestHeader(String name) {
        return requestContext.getHeaderString(name);
    }

    @Override
    public Map<String, Set<String>> getRequestParameters() {
        return parameters;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getBaseUrl() {
        return UrlUtils.removeTrailingSlash(requestContext.getUriInfo().getBaseUri().toString());
    }

    @Override
    public byte[] getRequestBody() {
        if (!requestBody.isPresent()) {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                InputStream is = requestContext.getEntityStream();
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                requestBody = Nullable.of(buffer.toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return requestBody.get();
    }


    @Override
    public String getMethod() {
        return requestContext.getMethod();
    }


    public void checkAbort() {
        if (response != null && response.getStatusCode() != 0) {
            Response.ResponseBuilder builder = Response.status(response.getStatusCode());

            if (response.getBody() != null) {
                builder = builder.entity(new ByteArrayInputStream(response.getBody()));
            }

            for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }

            requestContext.abortWith(builder.build());
        }
    }

    private Map<String, Set<String>> getParameters(UriInfo uriInfo) {
        MultivaluedMap<String, String> queryParametersMultiMap = uriInfo.getQueryParameters();
        Map<String, Set<String>> queryParameters = new HashMap<>();

        for (Map.Entry<String, List<String>> queryEntry : queryParametersMultiMap.entrySet()) {
            queryParameters.put(queryEntry.getKey(), new LinkedHashSet<>(queryEntry.getValue()));
        }
        return queryParameters;
    }

    private String buildPath(UriInfo uriInfo) {
        String basePath = uriInfo.getPath();
        String webPathPrefix = feature.getWebPathPrefix();
        String path;
        if (webPathPrefix != null && basePath.startsWith(webPathPrefix)) {
            path = basePath.substring(webPathPrefix.length());
        } else {
            path = basePath;
        }
        return path == null || path.isEmpty() ? "/" : path;
    }

}