package io.crnk.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("crnk")
public class CrnkSpringBootProperties {
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

	public String getResourcePackage() {
		return resourcePackage;
	}

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
}
