package io.crnk.spring.setup.boot.meta;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for crnk-meta
 */
@ConfigurationProperties("crnk.monitor.tracing")
public class CrnkTracingProperties {

	private boolean enabled = true;

	private boolean useSimpleTransactionNames = false;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return if enabled will not include any special characters in the transaction names at the cost of readablity
	 */
	public boolean getUseSimpleTransactionNames() {
		return useSimpleTransactionNames;
	}

	public void setUseSimpleTransactionNames(boolean useSimpleTransactionNames) {
		this.useSimpleTransactionNames = useSimpleTransactionNames;
	}
}
