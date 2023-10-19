package io.crnk.servlet;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.servlet.internal.FilterPropertiesProvider;
import io.crnk.servlet.internal.ServletModule;
import io.crnk.servlet.internal.ServletRequestContext;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter class to integrate with Crnk.
 * <p>
 * <p>
 * Child class can override {@link #initCrnk(CrnkBoot)} method and make use of CrnkBookt for further customizations.
 * </p>
 */
public class CrnkFilter implements Filter {

	protected CrnkBoot boot;

	private FilterConfig filterConfig;

	private String defaultCharacterEncoding = HttpHeaders.DEFAULT_CHARSET;

	public CrnkFilter() {

	}

	public CrnkFilter(CrnkBoot boot) {
		this.boot = boot;
	}

	@Override
	public void init(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;

		if (boot == null) {
			boot = new CrnkBoot();
			boot.setPropertiesProvider(new FilterPropertiesProvider(filterConfig));

			HttpRequestContextProvider provider = boot.getModuleRegistry().getHttpRequestContextProvider();
			boot.addModule(new ServletModule(provider));
			initCrnk(boot);
			boot.boot();
		}
	}

	public String getDefaultCharacterEncoding() {
		return defaultCharacterEncoding;
	}

	public void setDefaultCharacterEncoding(String defaultCharacterEncoding) {
		this.defaultCharacterEncoding = defaultCharacterEncoding;
	}

	protected void initCrnk(CrnkBoot boot) {
		// nothing to do here
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest && res instanceof HttpServletResponse && matchesPrefix((HttpServletRequest) req)) {
			ServletContext servletContext = filterConfig.getServletContext();
			ServletRequestContext context = new ServletRequestContext(servletContext, (HttpServletRequest) req,
					(HttpServletResponse) res, boot.getWebPathPrefix(), defaultCharacterEncoding);
			RequestDispatcher requestDispatcher = boot.getRequestDispatcher();
			requestDispatcher.process(context);
			if (!context.checkAbort()) {
				chain.doFilter(req, res);
			}
		} else {
			chain.doFilter(req, res);
		}
	}

	private boolean matchesPrefix(HttpServletRequest request) {
		String pathPrefix = UrlUtils.removeLeadingSlash(boot.getWebPathPrefix());
		String path = UrlUtils.removeLeadingSlash(request.getRequestURI().substring(request.getContextPath().length()));
		return pathPrefix == null || path.startsWith(pathPrefix);
	}

}
