package io.crnk.spring.setup.boot.operations;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-operations
 */
@ConfigurationProperties("crnk.operations")
public class CrnkOperationsProperties {

	private boolean enabled = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
