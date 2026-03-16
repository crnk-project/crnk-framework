package io.crnk.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.ServletContext;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.servlet.internal.ServletRequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ServletRequestContextTest {

	private ServletContext servletContext;

	private MockHttpServletRequest servletRequest;

	private MockHttpServletResponse servletResponse;

	@Before
	public void setup() {
		servletContext = Mockito.mock(ServletContext.class);

		servletRequest = new MockHttpServletRequest(servletContext);
		servletRequest.setMethod("GET");
		servletRequest.setContextPath("");
		servletRequest.setServerPort(1234);
		servletRequest.setRequestURI("/api/tasks/");
		servletRequest.setServerName("test");

		servletResponse = new MockHttpServletResponse();
	}

	@Test
	public void testGetters() {
		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, "/api");
		Assert.assertEquals(servletRequest, context.getServletRequest());
		Assert.assertEquals(servletResponse, context.getServletResponse());
		Assert.assertEquals(servletContext, context.getServletContext());
		Assert.assertEquals("http://test:1234/api/tasks/", context.getRequestUri().toString());
	}

	@Test
	public void testResponseHeaders() {
		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, "/api");

		HttpResponse response = new HttpResponse();
		response.setHeader("test", "13");
		context.setResponse(response);

		Assert.assertEquals("13", context.getResponse().getHeader("test"));
	}

	@Test
	public void testGetUrlWithServletPath() {
		servletRequest.setServletPath("/api");

		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, null);
		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}

	@Test
	public void testGetUrlWithServletPathWithFowardProto() {
		servletRequest.setServletPath("/api");

		servletRequest.addHeader(HttpHeaders.X_FORWARDED_PROTO_HEADER, "https");

		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, null);
		Assert.assertEquals("https://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
		Assert.assertEquals("https", context.getRequestHeader(HttpHeaders.X_FORWARDED_PROTO_HEADER));
		Assert.assertTrue(context.getRequestHeaderNames().contains(HttpHeaders.X_FORWARDED_PROTO_HEADER));
		Assert.assertTrue(context.getReadForwardedHeader());

		context = new ServletRequestContext(servletContext, servletRequest, servletResponse, null);
		context.setReadForwardedHeader(false);
		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
	}

	@Test
	public void testGetUrlWithPathPrefixOverridingServletPath() {
		servletRequest.setServletPath("");

		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, "/api/");
		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}


	@Test
	public void testPathPrefixNormalizedOnMissingLeadingSlash() {
		servletRequest.setServletPath("");

		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, "api/");
		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}

	@Test
	public void testPathPrefixNormalizedOnMissingTrailingSlash() {
		servletRequest.setServletPath("");

		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, "/api");
		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}

	@Test
	public void testGetUrlWithServletAndContextPath() {
		servletRequest.setContextPath("context");
		servletRequest.setServletPath("/context/servlet");
		servletRequest.setRequestURI("/context/servlet/tasks/");

		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, null);
		Assert.assertEquals("http://test:1234/context/servlet", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}

	@Test
	public void testGetUrlWithPathPrefixAndContextPath() {
		servletRequest.setContextPath("context");
		servletRequest.setServletPath("/context/servlet");
		servletRequest.setRequestURI("/context/api/tasks/");

		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, "/api/");
		Assert.assertEquals("http://test:1234/context/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}


	@Test
	public void checkCompletedOnSuccessStatus() throws IOException {
		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, "/api/");
		HttpResponse response = context.getResponse();
		response.setBody("test");
		response.setStatusCode(200);
		boolean completed = context.checkAbort();
		Assert.assertTrue(completed);
		Assert.assertEquals("4", servletResponse.getHeader("Content-Length"));
	}


	@Test
	public void testGetUrlWithEmptyServletPath() {
		servletRequest.setContextPath("/");
		servletRequest.setServletPath("/");
		servletRequest.setRequestURI("/tasks");

		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, null);
		Assert.assertEquals("http://test:1234", context.getBaseUrl());
		Assert.assertEquals("/tasks", context.getPath());
	}


	@Test
	public void testPathInfoShortcutsComputation() {
		servletRequest.setContextPath("context");
		servletRequest.setServletPath("/context/servlet");
		servletRequest.setRequestURI("/context/api/tasks/");
		servletRequest.setPathInfo("/api/something/");

		ServletRequestContext context = new ServletRequestContext(servletContext, servletRequest, servletResponse, "/api/");
		Assert.assertEquals("http://test:1234/context/api", context.getBaseUrl());
		Assert.assertEquals("/something/", context.getPath());
	}

	@Test
	public void testParameter() {
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.addParameter("include[test]", "a,b,c");

		ServletRequestContext context = new ServletRequestContext(servletContext, request, servletResponse, null);
		Map<String, Set<String>> parameters = context.getRequestParameters();
		Assert.assertEquals(1, parameters.size());
		Set<String> values = parameters.get("include[test]");
		Assert.assertEquals(1, values.size());
		Assert.assertEquals("a,b,c", values.iterator().next());
	}

	@Test
	public void testRootPath() {
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath("");
		request.setPathInfo("");
		request.setRequestURI("");
		request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
		request.addHeader("Accept", "*/*");
		request.setServerName("test");
		request.setServerPort(1234);

		ServletRequestContext context = new ServletRequestContext(servletContext, request, servletResponse, null);

		Assert.assertEquals("/", context.getPath());
		Assert.assertEquals("http://test:1234", context.getBaseUrl());
	}

	@Test
	public void testBaseUrlForInvalidServletPath() {
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath("/api/tasks/"); // invalid => Spring Boot setup as it seems
		request.setPathInfo(null);
		request.setRequestURI("/api/tasks/");
		request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
		request.setServerName("test");
		request.setServerPort(1234);

		ServletRequestContext context = new ServletRequestContext(servletContext, request, servletResponse, "/api");

		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}
}
