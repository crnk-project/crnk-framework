package io.crnk.client.http.inmemory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterListener;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.internal.utils.PreconditionUtil;

/**
 * Allows to connect a client to a server directly in memory without HTTP communication. Useful to write
 * performant unit tests.
 */
public class InMemoryHttpAdapter implements HttpAdapter {

	private final CrnkBoot boot;

	private final String baseUrl;

	private CopyOnWriteArrayList<HttpAdapterListener> listeners = new CopyOnWriteArrayList<>();

	public InMemoryHttpAdapter(CrnkBoot boot, String baseUrl) {
		this.boot = boot;
		this.baseUrl = baseUrl;
	}

	@Override
	public void addListener(HttpAdapterListener listener) {
		listeners.add(listener);
	}

	@Override
	public HttpAdapterRequest newRequest(String url, HttpMethod method, String requestBody) {
		return new InMemoryRequest(url, method, requestBody);
	}

	@Override
	public void setReceiveTimeout(int timeout, TimeUnit unit) {
	}

	class InMemoryResponse implements HttpAdapterResponse {

		private final HttpResponse response;

		public InMemoryResponse(HttpResponse response) {
			this.response = response;
		}

		@Override
		public boolean isSuccessful() {
			return response.getStatusCode() >= 200 && response.getStatusCode() <= 299;
		}

		@Override
		public String body() {
			byte[] body = response.getBody();
			return body != null ? new String(body, StandardCharsets.UTF_8) : null;
		}

		@Override
		public int code() {
			return response.getStatusCode();
		}

		@Override
		public String message() {
			return response.getStatusMessage();
		}

		@Override
		public String getResponseHeader(String name) {
			return response.getHeader(name);
		}

		@Override
		public Set<String> getHeaderNames() {
			return response.getHeaders().keySet();
		}
	}

	class ServerRequestContext implements HttpRequestContextBase {

		private final InMemoryRequest request;

		private HttpResponse response;

		public ServerRequestContext(InMemoryRequest request) {
			this.request = request;
		}

		@Override
		public String getRequestHeader(String name) {
			return request.getHeaderValue(name);
		}

		@Override
		public Map<String, Set<String>> getRequestParameters() {
			try {
				URL url = new URL(request.getUrl());
				Map<String, Set<String>> parameters = new LinkedHashMap<>();
				String query = url.getQuery();
				if (query != null) {
					String[] pairs = query.split("&");
					for (String pair : pairs) {
						int idx = pair.indexOf("=");
						String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
						String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
						Set<String> values = parameters.getOrDefault(key, new HashSet());
						values.add(value);
						parameters.put(key, values);
					}
				}
				return parameters;
			}
			catch (Exception e) {
				throw new IllegalStateException("failed to parse url: " + request.getUrl(), e);
			}
		}

		@Override
		public String getPath() {
			String url = request.getUrl();
			PreconditionUtil.verify(url.startsWith(baseUrl), "url " + url + " does not start with expected " + baseUrl);
			String path = url.substring(baseUrl.length());
			int sep = path.indexOf("?");
			if (sep != -1) {
				path = path.substring(0, sep);
			}
			return path;
		}

		@Override
		public String getBaseUrl() {
			return baseUrl;
		}

		@Override
		public byte[] getRequestBody() {
			String body = request.getBody();
			return body != null ? body.getBytes(StandardCharsets.UTF_8) : null;
		}

		@Override
		public String getMethod() {
			return request.getHttpMethod().toString();
		}

		@Override
		public URI getRequestUri() {
			return URI.create(request.getUrl());
		}

		@Override
		public HttpResponse getResponse() {
			return response;
		}

		@Override
		public void setResponse(HttpResponse response) {
			this.response = response;
		}
	}

	class InMemoryRequest implements HttpAdapterRequest {

		private final String url;

		private final HttpMethod method;

		private final String requestBody;

		private Map<String, String> headers = new HashMap<>();

		public InMemoryRequest(String url, HttpMethod method, String requestBody) {
			this.url = url;
			this.method = method;
			this.requestBody = requestBody;
		}

		@Override
		public void header(String name, String value) {
			headers.put(name, value);
		}

		@Override
		public HttpAdapterResponse execute() throws IOException {
			listeners.stream().forEach(it -> it.onRequest(this));

			ServerRequestContext serverRequestContext = new ServerRequestContext(this);

			HttpRequestDispatcherImpl requestDispatcher = boot.getRequestDispatcher();
			requestDispatcher.process(serverRequestContext);

			if (serverRequestContext.response == null) {
				HttpResponse notFoundResponse = new HttpResponse();
				notFoundResponse.setStatusCode(404);
				serverRequestContext.setResponse(notFoundResponse);
			}

			InMemoryResponse adapterResponse = new InMemoryResponse(serverRequestContext.response);
			listeners.stream().forEach(it -> it.onResponse(this, adapterResponse));
			return adapterResponse;
		}

		@Override
		public String getBody() {
			return requestBody;
		}

		@Override
		public String getUrl() {
			return url;
		}

		@Override
		public HttpMethod getHttpMethod() {
			return method;
		}

		@Override
		public Set<String> getHeadersNames() {
			return headers.keySet();
		}

		@Override
		public String getHeaderValue(String name) {
			return headers.get(name);
		}
	}
}
