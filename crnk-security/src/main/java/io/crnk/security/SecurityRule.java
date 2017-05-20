package io.crnk.security;

/**
 * Specifies a security rule to be applied.
 */
public class SecurityRule {

	private Class<?> resourceClass;

	private String role;

	private ResourcePermission permission;

	private String resourceType;

	public SecurityRule(String role, ResourcePermission permission) {
		this.role = role;
		this.permission = permission;
	}

	public SecurityRule(Class<?> resourceClass, String role, ResourcePermission permission) {
		this.resourceClass = resourceClass;
		this.role = role;
		this.permission = permission;
	}

	public SecurityRule(String resourceType, String role, ResourcePermission permission) {
		this.resourceType = resourceType;
		this.role = role;
		this.permission = permission;
	}

	/**
	 * @return type of the resource is rule applies to. It might be specified as String or Class. See {@link #getResourceClass()}.
	 */
	public String getResourceType() {
		return resourceType;
	}

	/**
	 * @return type of the resource is rule applies to.  It might be specified as String or Class. See {@link #getResourceType()}.
	 */
	public Class<?> getResourceClass() {
		return resourceClass;
	}

	/**
	 * @return name of the role this rule applies to or null if it applies to all users.
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @return permissions applied by this rule.
	 */
	public ResourcePermission getPermission() {
		return permission;
	}
}
