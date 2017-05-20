package io.crnk.security;

import io.crnk.core.resource.meta.MetaInformation;

/**
 * Can be implemented by MetaInformation classes to let the SecurityModule fill in the permissions.
 */
public interface ResourcePermissionInformation extends MetaInformation {

	ResourcePermission getResourcePermission();

	/**
	 * Filled in by the {@link SecurityModule} if null.
	 *
	 * @param resourcePermission for the requested resources.
	 */
	void setResourcePermission(ResourcePermission resourcePermission);

}
