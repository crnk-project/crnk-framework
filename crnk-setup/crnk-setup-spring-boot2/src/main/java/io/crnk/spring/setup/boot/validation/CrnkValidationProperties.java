package io.crnk.spring.setup.boot.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-validation
 */
@ConfigurationProperties("crnk.validation")
public class CrnkValidationProperties {

	private boolean enabled = true;

	private boolean validateResources = true;

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getValidateResources() {
		return validateResources;
	}

	public void setValidateResources(boolean validateResources) {
		this.validateResources = validateResources;
	}
}
