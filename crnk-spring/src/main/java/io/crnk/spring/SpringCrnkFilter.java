package io.crnk.spring;

import java.io.IOException;
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

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.servlet.internal.ServletRequestContext;

@Priority(20)
public class SpringCrnkFilter implements Filter {

	private CrnkBoot boot;

	private FilterConfig filterConfig;

	public SpringCrnkFilter(CrnkBoot boot) {
		this.boot = boot;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
			ServletContext servletContext = filterConfig.getServletContext();
			RequestDispatcher requestDispatcher = boot.getRequestDispatcher();
			ServletRequestContext context = new ServletRequestContext(servletContext, (HttpServletRequest) req,
					(HttpServletResponse) res, boot.getWebPathPrefix());
			requestDispatcher.process(context);
			if (!context.checkAbort()) {
				chain.doFilter(req, res);
			}
		}
		else {
			chain.doFilter(req, res);
		}
	}

	@Override
	public void destroy() {
		// nothing to do
	}
}

