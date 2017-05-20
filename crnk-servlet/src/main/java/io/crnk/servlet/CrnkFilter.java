/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.crnk.servlet;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.servlet.internal.FilterPropertiesProvider;
import io.crnk.servlet.internal.ServletModule;
import io.crnk.servlet.internal.ServletRequestContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;

		boot = new CrnkBoot();
		boot.setPropertiesProvider(new FilterPropertiesProvider(filterConfig));

		HttpRequestContextProvider provider = (HttpRequestContextProvider) boot.getDefaultServiceUrlProvider();
		boot.addModule(new ServletModule(provider));
		initCrnk(boot);
		boot.boot();
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
		if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
			ServletContext servletContext = filterConfig.getServletContext();
			ServletRequestContext context = new ServletRequestContext(servletContext, (HttpServletRequest) req,
					(HttpServletResponse) res, boot.getWebPathPrefix());
			RequestDispatcher requestDispatcher = boot.getRequestDispatcher();
			requestDispatcher.process(context);
			if (!context.checkAbort()) {
				chain.doFilter(req, res);
			}
		} else {
			chain.doFilter(req, res);
		}
	}

}
