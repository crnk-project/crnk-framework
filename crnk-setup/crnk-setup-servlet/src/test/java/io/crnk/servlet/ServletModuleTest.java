package io.crnk.servlet;

import javax.servlet.ServletContext;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.result.ImmediateResultFactory;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.servlet.internal.ServletModule;
import io.crnk.servlet.internal.ServletRequestContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ServletModuleTest {

	@Test
	public void testName() {
		ImmediateResultFactory resultFactory = new ImmediateResultFactory();
		HttpRequestContextProvider provider = new HttpRequestContextProvider(() -> resultFactory, null);
		ServletModule module = new ServletModule(provider);
		Assert.assertEquals("servlet", module.getModuleName());
	}

	@Test
	public void testSecurityProviderInstalled() {
		ImmediateResultFactory resultFactory = new ImmediateResultFactory();

		CrnkBoot boot = new CrnkBoot();
		HttpRequestContextProvider provider = new HttpRequestContextProvider(() -> resultFactory, boot.getModuleRegistry());
		ServletModule module = new ServletModule(provider);
		boot.addModule(module);
		boot.boot();

		SecurityProvider securityProvider = boot.getModuleRegistry().getSecurityProvider();
		ServletContext servletContext = Mockito.mock(ServletContext.class);
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		request.setRequestURI("/api/tasks");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addUserRole("guest");
		request.addUserRole("admin");

		provider.onRequestStarted(new HttpRequestContextBaseAdapter(new ServletRequestContext(servletContext, request,
				response, "api", HttpHeaders.DEFAULT_CHARSET)));

		Assert.assertFalse(securityProvider.isAuthenticated(null));
		Assert.assertFalse(securityProvider.isUserInRole("doesNotExist", null));
		Assert.assertTrue(securityProvider.isUserInRole("guest", null));
		Assert.assertTrue(securityProvider.isUserInRole("admin", null));
	}
}
