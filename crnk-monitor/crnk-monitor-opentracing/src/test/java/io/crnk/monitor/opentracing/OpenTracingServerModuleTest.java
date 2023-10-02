package io.crnk.monitor.opentracing;

import java.io.IOException;
import jakarta.servlet.ServletException;

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

	private boolean simpleTransactionNames = false;

	@Before
	public void setup() throws ServletException {
		simpleTransactionNames = false;

		tracer = Mockito.mock(Tracer.class);
		MockServletContext servletContext = new MockServletContext();

		initModule();

		request = new MockHttpServletRequest(servletContext);
		request.setMethod("GET");
		request.setContextPath("");
		request.setServletPath("/api");
		request.setPathInfo("/tasks/");
		request.setRequestURI("/api/tasks/");
		request.setContentType(HttpHeaders.JSONAPI_CONTENT_TYPE);
		request.addHeader("Accept", "*/*");
	}

	private void initModule() throws ServletException {
		OpenTracingServerModule module = new OpenTracingServerModule(tracer);
		module.setUseSimpleTransactionNames(simpleTransactionNames);

		servlet = new CrnkServlet();
		CrnkBoot boot = servlet.getBoot();
		boot.addModule(new TestModule());
		boot.addModule(module);
		servlet.init(new MockServletConfig());
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
	public void testWithSimpleTransactionNames() throws ServletException, IOException {
		simpleTransactionNames = true;
		initModule();

		Span span = Mockito.mock(Span.class);
		Mockito.when(tracer.activeSpan()).thenReturn(span);

		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);

		Mockito.verify(span, Mockito.times(1)).setOperationName(Mockito.eq("GET_apiTasks"));
	}

	@Test
	public void testWithoutSpan() throws ServletException, IOException {
		MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.service(request, response);
		Mockito.verify(tracer, Mockito.times(1)).activeSpan();
	}
}
