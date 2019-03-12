package io.crnk.monitor.opentracing;

import java.io.IOException;
import javax.servlet.ServletException;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.servlet.CrnkServlet;
import io.crnk.test.mock.TestModule;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public class OpenTracingServerModuleTest {

	private Tracer tracer;

	private CrnkServlet servlet;

	private MockHttpServletRequest request;

	@Before
	public void setup() throws ServletException {
		tracer = Mockito.mock(Tracer.class);
		MockServletContext servletContext = new MockServletContext();
		servlet = new CrnkServlet();

		CrnkBoot boot = servlet.getBoot();
		boot.addModule(new TestModule());
		boot.addModule(new OpenTracingServerModule(tracer));
		servlet.init(new MockServletConfig());

		request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath("/api");
		request.setPathInfo("/tasks/");
		request.setRequestURI("/api/tasks/");
		request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
		request.addHeader("Accept", "*/*");
	}

	@Test
	public void testWithSpan() throws ServletException, IOException {
		Span span = Mockito.mock(Span.class);
		Mockito.when(tracer.activeSpan()).thenReturn(span);

		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		Mockito.verify(span, Mockito.times(1)).setOperationName(Mockito.eq("GET /api/tasks"));
	}

	@Test
	public void testWithoutSpan() throws ServletException, IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		Mockito.verify(tracer, Mockito.times(1)).activeSpan();
	}
}
