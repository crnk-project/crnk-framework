package io.crnk.spring.setup.boot.ui;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-ui
 */
@ConfigurationProperties("crnk.ui")
public class CrnkUiProperties {

	private boolean enabled = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
