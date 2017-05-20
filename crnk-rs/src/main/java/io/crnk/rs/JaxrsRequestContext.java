package io.crnk.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;
import io.crnk.rs.internal.parameterProvider.JaxRsParameterProvider;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class JaxrsRequestContext implements HttpRequestContextBase {

	private final CrnkFeature feature;

	private ContainerRequestContext requestContext;

	private String path;

	private Map<String, Set<String>> parameters;

	private Map<String, String> responseHeaders = new HashMap<>();

	private Nullable<byte[]> requestBody = Nullable.empty();

	private Integer responseCode;

	private byte[] responseBody;

	private RepositoryMethodParameterProvider requestParameterProvider;

	JaxrsRequestContext(ContainerRequestContext requestContext, CrnkFeature feature) {
		this.feature = feature;
		this.requestContext = requestContext;

		UriInfo uriInfo = requestContext.getUriInfo();
		this.path = buildPath(uriInfo);
		this.parameters = getParameters(uriInfo);

		ObjectMapper objectMapper = feature.getBoot().getObjectMapper();
		requestParameterProvider =
				new JaxRsParameterProvider(objectMapper, requestContext, feature.getParameterProviderRegistry());
	}

	@Override
	public String getResponseHeader(String name) {
		return responseHeaders.get(name);
	}

	@Override
	public RepositoryMethodParameterProvider getRequestParameterProvider() {
		return requestParameterProvider;
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
		String url = requestContext.getUriInfo().getBaseUri().toString();
		if (url.endsWith("/")) {
			return url.substring(0, url.length() - 1);
		} else {
			return url;
		}
	}

	@Override
	public byte[] getRequestBody() throws IOException {
		if (!requestBody.isPresent()) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			InputStream is = requestContext.getEntityStream();
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			requestBody = Nullable.of(buffer.toByteArray());
		}
		return requestBody.get();
	}

	@Override
	public void setResponseHeader(String name, String value) {
		this.responseHeaders.put(name, value);
	}

	@Override
	public void setResponse(int code, byte[] body) {
		this.responseCode = code;
		this.responseBody = body;
	}

	@Override
	public String getMethod() {
		return requestContext.getMethod();
	}


	public void checkAbort() {
		if (responseCode != null) {
			Response.ResponseBuilder builder = Response.status(responseCode);

			if (responseBody != null) {
				builder = builder.entity(new ByteArrayInputStream(responseBody));
			}

			for (Map.Entry<String, String> entry : responseHeaders.entrySet()) {
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
		if (webPathPrefix != null && basePath.startsWith(webPathPrefix)) {
			return basePath.substring(webPathPrefix.length());
		} else {
			return basePath;
		}
	}

}