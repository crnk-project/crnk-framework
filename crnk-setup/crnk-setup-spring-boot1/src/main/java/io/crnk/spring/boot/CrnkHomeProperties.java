package io.crnk.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-home
 */
@ConfigurationProperties("crnk.home")
public class CrnkHomeProperties {

	private boolean enabled = true;

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
