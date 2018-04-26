package io.crnk.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-spring-mvc
 */
@ConfigurationProperties("crnk.spring.mvc")
public class CrnkSpringMvcProperties {

	private boolean enabled = true;

	private boolean errorController = true;

	/**
	 * @return true if SpringMvcModule should be used.
	 */
	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return true if JSON errors should be serialized in JSON API format
	 */
	public boolean isErrorController() {
		return errorController;
	}

	public void setErrorController(boolean errorController) {
		this.errorController = errorController;
	}
}
