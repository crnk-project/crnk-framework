package io.crnk.servlet.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.servlet.internal.legacy.ServletParametersProvider;

public class ServletRequestContext implements HttpRequestContextBase {


	private final HttpServletRequest servletRequest;

	private final HttpServletResponse servletResponse;

	private final ServletParametersProvider parameterProvider;

	private final ServletContext servletContext;

	private final String defaultCharacterEncoding;

	private Map<String, Set<String>> parameters;

	private Nullable<byte[]> requestBody = Nullable.empty();

	private HttpResponse response = new HttpResponse();

	private String pathPrefix;

	public ServletRequestContext(final ServletContext servletContext, final HttpServletRequest request,
								 final HttpServletResponse response, String pathPrefix) {
		this(servletContext, request, response, pathPrefix, HttpHeaders.DEFAULT_CHARSET);
	}

	public ServletRequestContext(final ServletContext servletContext, final HttpServletRequest request,
								 final HttpServletResponse response, String pathPrefix, String defaultCharacterEncoding) {
		this.pathPrefix = pathPrefix;
		this.servletContext = servletContext;
		this.servletRequest = request;
		this.servletResponse = response;
		this.parameterProvider = new ServletParametersProvider(servletContext, request, response);
		this.defaultCharacterEncoding = Objects.requireNonNull(defaultCharacterEncoding);
		this.parameters = getParameters(request);
	}


	public boolean checkAbort() throws IOException {
		if (response.getStatusCode() > 0) {
			servletResponse.setStatus(response.getStatusCode());
			response.getHeaders().forEach((key, value) -> servletResponse.setHeader(key, value));
			if (response.getBody() != null) {
				OutputStream out = servletResponse.getOutputStream();
				out.write(response.getBody());
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
		return queryParameters;
	}


	@Override
	public RepositoryMethodParameterProvider getRequestParameterProvider() {
		return parameterProvider;
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
		String path = servletRequest.getPathInfo();

		// Serving with Filter, pathInfo can be null.
		if (path == null) {
			path = servletRequest.getRequestURI().substring(servletRequest.getContextPath().length());
		}

		if (pathPrefix != null && path.startsWith(pathPrefix)) {
			path = path.substring(pathPrefix.length());
		}

		if (path.isEmpty()) {
			return "/";
		}

		return path;
	}

	@Override
	public String getBaseUrl() {
		String requestUrl = UrlUtils.removeTrailingSlash(servletRequest.getRequestURL().toString());
		String servletPath = UrlUtils.removeTrailingSlash(servletRequest.getServletPath());

		if (pathPrefix != null && servletPath.startsWith(pathPrefix)) {
			// harden again invalid servlet paths (e.g. in case of filters)
			servletPath = pathPrefix;
		}
		if (servletPath.isEmpty()) {
			return UrlUtils.removeTrailingSlash(requestUrl);
		}

		int sep = requestUrl.indexOf(servletPath);

		String url = requestUrl.substring(0, sep + servletPath.length());
		return UrlUtils.removeTrailingSlash(url);
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
	public void setResponseHeader(String name, String value) {
		response.setHeader(name, value);
	}

	@Override
	public void setResponse(int code, byte[] body) {
		response.setStatusCode(code);
		response.setBody(body);
	}

	@Override
	public String getMethod() {
		return servletRequest.getMethod().toUpperCase();
	}

	@Override
	public String getResponseHeader(String name) {
		return response.getHeader(name);
	}

	@Override
	public HttpResponse getResponse() {
		return response;
	}

	@Override
	public void setResponse(HttpResponse response) {
		this.response = response;
	}

	/**
	 * @deprecated use {{@link #getResponseHeader(String)}}
	 */
	@Deprecated
	public HttpServletRequest getRequest() {
		return servletRequest;
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
