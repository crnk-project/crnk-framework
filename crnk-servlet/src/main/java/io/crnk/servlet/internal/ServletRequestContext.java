/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.crnk.servlet.internal;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.servlet.internal.legacy.ServletParametersProvider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class ServletRequestContext implements HttpRequestContextBase {


	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final ServletParametersProvider parameterProvider;

	private final ServletContext servletContext;

	private final String defaultCharacterEncoding;

	private Map<String, Set<String>> parameters;

	private Nullable<byte[]> requestBody = Nullable.empty();

	private boolean hasResponse;

	private String pathPrefix;

	public ServletRequestContext(final ServletContext servletContext, final HttpServletRequest request,
								 final HttpServletResponse response, String pathPrefix) {
		this(servletContext, request, response, pathPrefix, HttpHeaders.DEFAULT_CHARSET);
	}

	public ServletRequestContext(final ServletContext servletContext, final HttpServletRequest request,
								 final HttpServletResponse response, String pathPrefix, String defaultCharacterEncoding) {
		this.pathPrefix = pathPrefix;
		this.servletContext = servletContext;
		this.request = request;
		this.response = response;
		this.parameterProvider = new ServletParametersProvider(servletContext, request, response);
		this.defaultCharacterEncoding = Objects.requireNonNull(defaultCharacterEncoding);
		this.parameters = getParameters(request);
	}


	public boolean checkAbort() {
		return hasResponse;
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
		return request.getHeader(name);
	}

	@Override
	public Map<String, Set<String>> getRequestParameters() {
		return parameters;
	}

	@Override
	public String getPath() {
		String path = request.getPathInfo();

		// Serving with Filter, pathInfo can be null.
		if (path == null) {
			path = request.getRequestURI().substring(request.getContextPath().length());
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
		String requestUrl = request.getRequestURL().toString();
		String servletPath = request.getServletPath();

		if (pathPrefix != null && servletPath.startsWith(pathPrefix)) {
			// harden again invalid servlet paths (e.g. in case of filters)
			servletPath = pathPrefix;
		} else if (servletPath.isEmpty()) {
			return UrlUtils.removeTrailingSlash(requestUrl);
		}

		int sep = requestUrl.indexOf(servletPath);

		String url = requestUrl.substring(0, sep + servletPath.length());
		return UrlUtils.removeTrailingSlash(url);
	}

	@Override
	public byte[] getRequestBody() throws IOException {
		if (!requestBody.isPresent()) {

			InputStream is = request.getInputStream();
			if (is != null) {
				requestBody = Nullable.of(io.crnk.core.engine.internal.utils.IOUtils.readFully(is));
			} else {
				requestBody = Nullable.nullValue();
			}
		}
		return requestBody.get();
	}

	@Override
	public void setResponseHeader(String name, String value) {
		PreconditionUtil.assertFalse("response set, cannot add further headers", hasResponse);
		response.setHeader(name, value);
	}

	@Override
	public void setResponse(int code, byte[] body) throws IOException {
		hasResponse = true;
		response.setStatus(code);
		if (body != null) {
			OutputStream out = response.getOutputStream();
			out.write(body);
			out.close();
		}
	}

	@Override
	public String getMethod() {
		return request.getMethod().toUpperCase();
	}

	@Override
	public String getResponseHeader(String name) {
		return response.getHeader(name);
	}


	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}
}
