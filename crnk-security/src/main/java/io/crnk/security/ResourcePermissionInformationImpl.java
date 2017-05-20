package io.crnk.security;

/**
 * Default implementation of {@link ResourcePermissionInformation}.
 */
public class ResourcePermissionInformationImpl implements ResourcePermissionInformation {

	private ResourcePermission resourcePermission;

	@Override
	public ResourcePermission getResourcePermission() {
		return resourcePermission;
	}

	@Override
	public void setResourcePermission(ResourcePermission resourcePermission) {
		this.resourcePermission = resourcePermission;
	}

}
