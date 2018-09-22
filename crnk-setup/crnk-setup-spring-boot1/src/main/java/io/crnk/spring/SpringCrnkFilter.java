package io.crnk.spring;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.internal.utils.UrlUtils;
import io.crnk.servlet.internal.ServletRequestContext;
import io.crnk.spring.boot.CrnkSpringBootProperties;

import javax.annotation.Priority;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @deprecated use CrnkFilter
 */
@Priority(20)
@Deprecated
public class SpringCrnkFilter implements Filter {

	private CrnkBoot boot;

	private FilterConfig filterConfig;

	private String defaultCharacterEncoding = HttpHeaders.DEFAULT_CHARSET;

	public SpringCrnkFilter(CrnkBoot boot, CrnkSpringBootProperties properties) {
		this.boot = boot;
	}

	@Override
	public void init(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest && res instanceof HttpServletResponse && matchesPrefix((HttpServletRequest) req)) {
			ServletContext servletContext = filterConfig.getServletContext();
			RequestDispatcher requestDispatcher = boot.getRequestDispatcher();
			ServletRequestContext context = new ServletRequestContext(servletContext, (HttpServletRequest) req,
					(HttpServletResponse) res, boot.getWebPathPrefix(), defaultCharacterEncoding);
			requestDispatcher.process(context);
			if (!context.checkAbort()) {
				chain.doFilter(req, res);
			}
		} else {
			chain.doFilter(req, res);
		}
	}

	public String getDefaultCharacterEncoding() {
		return defaultCharacterEncoding;
	}

	public void setDefaultCharacterEncoding(String defaultCharacterEncoding) {
		this.defaultCharacterEncoding = defaultCharacterEncoding;
	}

	private boolean matchesPrefix(HttpServletRequest request) {
		String pathPrefix = UrlUtils.removeLeadingSlash(boot.getWebPathPrefix());
		String path = UrlUtils.removeLeadingSlash(request.getRequestURI().substring(request.getContextPath().length()));
		return pathPrefix == null || path.startsWith(pathPrefix);
	}

	@Override
	public void destroy() {
		// nothing to do
	}
}

