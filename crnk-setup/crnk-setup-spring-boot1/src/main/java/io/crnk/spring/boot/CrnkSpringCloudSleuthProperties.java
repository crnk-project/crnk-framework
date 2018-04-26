package io.crnk.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-spring-cloud-sleuth
 */
@ConfigurationProperties("crnk.spring.cloud.sleuth")
public class CrnkSpringCloudSleuthProperties {

	private boolean enabled = true;

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
