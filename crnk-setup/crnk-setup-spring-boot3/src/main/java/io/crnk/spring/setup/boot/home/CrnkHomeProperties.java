package io.crnk.spring.setup.boot.home;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-home
 */
@ConfigurationProperties("crnk.home")
public class CrnkHomeProperties {

	private boolean enabled = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
