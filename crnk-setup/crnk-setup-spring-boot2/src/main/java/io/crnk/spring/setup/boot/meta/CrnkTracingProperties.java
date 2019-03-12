package io.crnk.spring.setup.boot.meta;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-meta
 */
@ConfigurationProperties("crnk.monitor.tracing")
public class CrnkTracingProperties {

	private boolean enabled = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
