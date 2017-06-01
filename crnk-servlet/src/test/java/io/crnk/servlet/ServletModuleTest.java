package io.crnk.servlet;

import javax.servlet.ServletContext;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
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
		HttpRequestContextProvider provider = new HttpRequestContextProvider();
		ServletModule module = new ServletModule(provider);
		Assert.assertEquals("servlet", module.getModuleName());
	}

	@Test
	public void testSecurityProviderInstalled() {
		HttpRequestContextProvider provider = new HttpRequestContextProvider();
		ServletModule module = new ServletModule(provider);

		CrnkBoot boot = new CrnkBoot();
		boot.addModule(module);
		boot.boot();

		SecurityProvider securityProvider = boot.getModuleRegistry().getSecurityProvider();
		ServletContext servletContext = Mockito.mock(ServletContext.class);
		MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
		MockHttpServletResponse response = new MockHttpServletResponse();

		request.addUserRole("guest");
		request.addUserRole("admin");

		provider.onRequestStarted(new HttpRequestContextBaseAdapter(new ServletRequestContext(servletContext, request,
				response, "api")));


		Assert.assertFalse(securityProvider.isUserInRole("doesNotExist"));
		Assert.assertTrue(securityProvider.isUserInRole("guest"));
		Assert.assertTrue(securityProvider.isUserInRole("admin"));
	}
}
