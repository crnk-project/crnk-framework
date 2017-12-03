package io.crnk.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("crnk")
public class CrnkSpringBootProperties {

	private boolean enabled = true;

	@Deprecated
	private String resourcePackage;

	/**
	 * The FQDN of the running server. It is used when building link objects in responses. The value must not end
	 * with /.
	 */
	private String domainName;

	/**
	 * Default prefix of a URL path used in two cases:
	 * <ul>
	 * <li>When building links objects in responses</li>
	 * <li>When performing method matching</li>
	 * </ul>
	 */
	private String pathPrefix;

	/**
	 * Default limit of pages.
	 */
	private Long defaultPageLimit;

	/**
	 * Maximum size of pages.
	 */
	private Long maxPageLimit;

	/**
	 * Allow unknown attributes in query parameters.
	 */
	private Boolean allowUnknownAttributes;

	/**
	 * Enable 404 response if null.
	 */
	private Boolean return404OnNull;

	public String getResourcePackage() {
		return resourcePackage;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @deprecated use dependency injection
	 */
	@Deprecated
	public void setResourcePackage(String resourcePackage) {
		this.resourcePackage = resourcePackage;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	public Long getDefaultPageLimit() {
		return defaultPageLimit;
	}

	public void setDefaultPageLimit(Long defaultPageLimit) {
		this.defaultPageLimit = defaultPageLimit;
	}

	public Long getMaxPageLimit() {
		return maxPageLimit;
	}

	public void setMaxPageLimit(Long maxPageLimit) {
		this.maxPageLimit = maxPageLimit;
	}

	public Boolean getAllowUnknownAttributes() {
		return allowUnknownAttributes;
	}

	public void setAllowUnknownAttributes(Boolean allowUnknownAttributes) {
		this.allowUnknownAttributes = allowUnknownAttributes;
	}

	public Boolean getReturn404OnNull() {
		return return404OnNull;
	}

	public void setReturn404OnNull(Boolean return404OnNull) {
		this.return404OnNull = return404OnNull;
	}
}
