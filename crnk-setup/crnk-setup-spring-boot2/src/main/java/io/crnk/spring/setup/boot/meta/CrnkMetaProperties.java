package io.crnk.spring.setup.boot.meta;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-meta
 */
@ConfigurationProperties("crnk.meta")
public class CrnkMetaProperties {

	private boolean enabled = true;

	private boolean listResources = true;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getListResources() {
		return listResources;
	}

	public void setListResources(boolean listResources) {
		this.listResources = listResources;
	}
}
