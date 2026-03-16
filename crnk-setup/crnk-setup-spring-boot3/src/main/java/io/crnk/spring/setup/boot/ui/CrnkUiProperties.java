package io.crnk.spring.setup.boot.ui;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-ui
 */
@ConfigurationProperties("crnk.ui")
public class CrnkUiProperties {

	private boolean enabled = true;

	private boolean browserEnabled = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isBrowserEnabled() {
		return browserEnabled;
	}

	public void setBrowserEnabled(boolean browserEnabled) {
		this.browserEnabled = browserEnabled;
	}
}
