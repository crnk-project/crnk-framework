package io.crnk.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.servlet.internal.ServletRequestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;
import java.util.Set;

public class ServletRequestContextTest {

	private ServletContext servletContext;

	private HttpServletRequest request;

	private HttpServletResponse response;

	@Before
	public void setup() {
		servletContext = Mockito.mock(ServletContext.class);
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
	}

	@Test
	public void testGetters() {
		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "/api");
		Assert.assertEquals(request, context.getRequest());
		Assert.assertEquals(response, context.getResponse());
		Assert.assertEquals(servletContext, context.getServletContext());
	}

	@Test
	public void testResponseHeaders() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "/api");

		context.setResponseHeader("test", "13");
		Assert.assertEquals("13", context.getResponseHeader("test"));
	}

	@Test
	public void testGetUrl() {
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath("/api");
		request.setPathInfo("/tasks/");
		request.setRequestURI("/api/tasks/");
		request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
		request.addHeader("Accept", "*/*");
		request.setServerName("test");
		request.setServerPort(1234);


		ServletRequestContext context = new ServletRequestContext(servletContext, request, response, "/api");

		Assert.assertEquals("http://test:1234/api", context.getBaseUrl());
		Assert.assertEquals("/tasks/", context.getPath());
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
