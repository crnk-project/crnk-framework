package io.crnk.ui.internal;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.utils.IOUtils;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.ui.UIModuleConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UIHttpRequestProcessor implements HttpRequestProcessor {

    private final UIModuleConfig config;

    private static final Map<String, String> CONTENT_TYPES = new HashMap<>();

    static {
        CONTENT_TYPES.put("css", "text/css");
        CONTENT_TYPES.put("html", "text/html");
        CONTENT_TYPES.put("js", "application/javascript");
    }

    public UIHttpRequestProcessor(UIModuleConfig config) {
        this.config = config;
    }

    @Override
    public void process(HttpRequestContext context) throws IOException {
        String pathPrefix = UrlUtils.removeLeadingSlash(UrlUtils.removeTrailingSlash(config.getPath())) + "/";
        String path = UrlUtils.removeLeadingSlash(context.getPath());

        if (context.getMethod().equals(HttpMethod.GET.toString()) && path.startsWith(pathPrefix)) {
            String fileName = path.substring(pathPrefix.length());
            if (fileName.isEmpty()) {
                fileName = "index.html";
            }
            String resourcePath = "io/crnk/ui/" + fileName;
            InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (in != null) {
                HttpResponse response = new HttpResponse();
                response.setStatusCode(200);
                response.setBody(IOUtils.readFully(in));
                response.setHeader("Cache-Control", "no-cache, no-store");
                setContentType(fileName, response);
                context.setResponse(response);
            }
        }
    }

    private static void setContentType(String fileName, HttpResponse response) {
        int suffixSep = fileName.lastIndexOf('.');
        if (suffixSep != -1) {
            String suffix = fileName.substring(suffixSep + 1);
            String contentType = CONTENT_TYPES.get(suffix);
            if (contentType != null) {
                response.setContentType(contentType);
            }
        }
    }
}
