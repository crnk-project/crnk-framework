package io.crnk.servlet.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.crnk.core.engine.http.DefaultHttpRequestContextBase;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.utils.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletRequestContext extends DefaultHttpRequestContextBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServletRequestContext.class);


	private final HttpServletRequest servletRequest;

	private final HttpServletResponse servletResponse;

	private final ServletContext servletContext;

	private final String defaultCharacterEncoding;

	private final String pathPrefix;

	private String path;

	private String baseUrl;

	private Map<String, Set<String>> parameters;

	private Nullable<byte[]> requestBody = Nullable.empty();

	private HttpResponse response = new HttpResponse();


	public ServletRequestContext(final ServletContext servletContext, final HttpServletRequest request,
			final HttpServletResponse response, String pathPrefix) {
		this(servletContext, request, response, pathPrefix, HttpHeaders.DEFAULT_CHARSET);
	}

	public ServletRequestContext(final ServletContext servletContext, final HttpServletRequest request,
			final HttpServletResponse response, String pathPrefix, String defaultCharacterEncoding) {
		this.servletContext = servletContext;
		this.servletRequest = request;
		this.servletResponse = response;
		this.defaultCharacterEncoding = Objects.requireNonNull(defaultCharacterEncoding);
		this.parameters = getParameters(request);
		this.pathPrefix = normalizePathPrefix(pathPrefix);
	}

	private String computePath(String baseUrl) {
		String path = servletRequest.getPathInfo();

		// Serving with Filter, pathInfo can be null.
		if (path == null || path.isEmpty()) { // spring seems to return empty string
			String requestUrl = getRequestUri().toString();
			if (!requestUrl.startsWith(baseUrl)) {
				throw new IllegalStateException("invalid base url: " + baseUrl + " for request " + requestUrl);
			}
			path = requestUrl.substring(baseUrl.length());
		} else if (pathPrefix != null && path.startsWith(pathPrefix)) {
			path = path.substring(pathPrefix.length());
		}

		if (path.isEmpty()) {
			return "/";
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		return path;
	}

	private String computeBaseUrl() {
		final URI requestUri = getRequestUri();

		String contextPath = UrlUtils.removeTrailingSlash(servletRequest.getContextPath());
		String servletPath = UrlUtils.removeTrailingSlash(servletRequest.getServletPath());

		String basePath;
		if (pathPrefix != null && pathPrefix.startsWith(contextPath)) {
			basePath = pathPrefix;
		} else if (pathPrefix != null) {
			basePath = contextPath + pathPrefix;
		} else if (servletPath != null) {
			basePath = servletPath;
		} else {
			basePath = contextPath;
		}
		basePath = UrlUtils.removeTrailingSlash(basePath);
		basePath = UrlUtils.removeLeadingSlash(basePath);

		LOGGER.debug("use basePath={} for contextPath={}, pathPrefix={}, servletPath={}, requestUrl=requestUrl", basePath,
				contextPath, pathPrefix, servletPath, requestUri);

		return requestUri.getScheme()
				+ "://" + requestUri.getHost()
				+ (requestUri.getPort() != -1 ? ":" + requestUri.getPort() : "")
				+ (basePath.length() > 0 ? "/" + basePath : "");
	}

	private static String normalizePathPrefix(String pathPrefix) {
		if (pathPrefix != null) {
			if (!pathPrefix.startsWith("/")) {
				pathPrefix = "/" + pathPrefix;
			}
			if (!pathPrefix.endsWith("/")) {
				pathPrefix = pathPrefix + "/";
			}
		}
		return pathPrefix;
	}


	public boolean checkAbort() throws IOException {
		if (response.getStatusCode() > 0) {
			servletResponse.setStatus(response.getStatusCode());
			response.getHeaders().forEach((key, value) -> servletResponse.setHeader(key, value));
			byte[] body = response.getBody();
			if (body != null) {
				servletResponse.setContentLength(body.length);

				OutputStream out = servletResponse.getOutputStream();
				out.write(body);
				out.close();
			}
			return true;
		}
		return false;
	}

	private Map<String, Set<String>> getParameters(HttpServletRequest request) {
		String characterEncoding = request.getCharacterEncoding();
		try {
			if (characterEncoding == null) {
				characterEncoding = defaultCharacterEncoding;
				request.setCharacterEncoding(characterEncoding);
				LOGGER.debug("setting default character encoding on servlet request: {}", characterEncoding);
			}
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}

		Map<String, Set<String>> queryParameters = new HashMap<>();
		for (Map.Entry<String, String[]> queryEntry : request.getParameterMap().entrySet()) {
			Set<String> paramValues = new LinkedHashSet();
			for (String paramValue : queryEntry.getValue()) {
				paramValues.add(paramValue);
			}
			queryParameters.put(queryEntry.getKey(), paramValues);
		}
		LOGGER.debug("obtained url parameters from servlet engine: {}", queryParameters);
		return queryParameters;
	}

	@Override
	public Set<String> getRequestHeaderNames() {
		return new HashSet<>(Collections.list(servletRequest.getHeaderNames()));
	}

	@Override
	public String getRequestHeader(String name) {
		return servletRequest.getHeader(name);
	}

	@Override
	public Map<String, Set<String>> getRequestParameters() {
		return parameters;
	}

	@Override
	public String getPath() {
		if (path == null) {
			path = computePath(getBaseUrl());
		}
		return path;
	}

	@Override
	public String getBaseUrl() {
		if (baseUrl == null) {
			baseUrl = computeBaseUrl();
		}
		return baseUrl;
	}

	@Override
	public byte[] getRequestBody() {
		if (!requestBody.isPresent()) {
			try {
				InputStream is = servletRequest.getInputStream();
				if (is != null) {
					requestBody = Nullable.of(io.crnk.core.engine.internal.utils.IOUtils.readFully(is));
				} else {
					requestBody = Nullable.nullValue();
				}
			} catch (IOException e) {
				throw new IllegalStateException(e); // FIXME
			}
		}
		return requestBody.get();
	}

	@Override
	public String getMethod() {
		return servletRequest.getMethod().toUpperCase();
	}

	@Override
	public URI getNativeRequestUri() {
		return URI.create(servletRequest.getRequestURL().toString());
	}

	@Override
	public HttpResponse getResponse() {
		return response;
	}

	@Override
	public void setResponse(HttpResponse response) {
		this.response = response;
	}

	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	public HttpServletResponse getServletResponse() {
		return servletResponse;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}
}
