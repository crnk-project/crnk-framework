package io.crnk.servlet.internal;

import io.crnk.core.engine.properties.PropertiesProvider;

import javax.servlet.ServletConfig;

public class ServletPropertiesProvider implements PropertiesProvider {

	private ServletConfig servletConfig;

	public ServletPropertiesProvider(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	@Override
	public String getProperty(String key) {
		return servletConfig.getInitParameter(key);
	}

}
