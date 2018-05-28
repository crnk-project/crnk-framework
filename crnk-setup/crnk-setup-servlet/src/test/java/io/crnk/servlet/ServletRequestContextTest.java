package io.crnk.servlet;

import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.servlet.internal.ServletRequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ServletRequestContextTest {

	private ServletContext servletContext;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	@Before
	public void setup() {
		servletContext = Mockito.mock(ServletContext.class);

		request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServerPort(1234);
		request.setRequestURI("/api/tasks/");
		request.setServerName("test");

		response = new MockHttpServletResponse();
	}

	@Test
	public void testGetters() {
		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "/api");
		Assert.assertEquals(request, context.getServletRequest());
		Assert.assertEquals(request, context.getRequest());
		Assert.assertEquals(response, context.getServletResponse());
		Assert.assertEquals(servletContext, context.getServletContext());
	}

	@Test
	public void testResponseHeaders() {
		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "/api");

		context.setResponseHeader("test", "13");
		Assert.assertEquals("13", context.getResponseHeader("test"));
	}

	@Test
	public void testGetUrlWithServletPath() {
		request.setServletPath("/api");

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, null);
		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}

	@Test
	public void testGetUrlWithPathPrefixOverridingServletPath() {
		request.setServletPath("");

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "/api/");
		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}


	@Test
	public void testPathPrefixNormalizedOnMissingLeadingSlash() {
		request.setServletPath("");

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "api/");
		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}

	@Test
	public void testPathPrefixNormalizedOnMissingTrailingSlash() {
		request.setServletPath("");

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "/api");
		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}

	@Test
	public void testGetUrlWithServletAndContextPath() {
		request.setContextPath("context");
		request.setServletPath("/context/servlet");
		request.setRequestURI("/context/servlet/tasks/");

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, null);
		Assert.assertEquals("http://test:1234/context/servlet", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}

	@Test
	public void testGetUrlWithPathPrefixAndContextPath() {
		request.setContextPath("context");
		request.setServletPath("/context/servlet");
		request.setRequestURI("/context/api/tasks/");

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "/api/");
		Assert.assertEquals("http://test:1234/context/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}


	@Test
	public void testGetUrlWithEmptyServletPath() {
		request.setContextPath("/");
		request.setServletPath("/");
		request.setRequestURI("/tasks");

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, null);
		Assert.assertEquals("http://test:1234", context.getBaseUrl());
		Assert.assertEquals("/tasks", context.getPath());
	}


	@Test
	public void testPathInfoShortcutsComputation() {
		request.setContextPath("context");
		request.setServletPath("/context/servlet");
		request.setRequestURI("/context/api/tasks/");
		request.setPathInfo("/api/something/");

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "/api/");
		Assert.assertEquals("http://test:1234/context/api", context.getBaseUrl());
		Assert.assertEquals("/something/", context.getPath());
	}

	@Test
	public void testParameter() {
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.addParameter("include[test]", "a,b,c");

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, null);
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

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, null);

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

		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "/api");

		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
	}
}
