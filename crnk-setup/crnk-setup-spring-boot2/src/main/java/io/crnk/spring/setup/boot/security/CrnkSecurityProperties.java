package io.crnk.spring.setup.boot.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-security
 */
@ConfigurationProperties("crnk.security")
public class CrnkSecurityProperties {

	private boolean enabled = true;

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
