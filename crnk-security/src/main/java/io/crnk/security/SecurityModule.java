package io.crnk.security;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.module.Module;
import io.crnk.core.utils.Supplier;
import io.crnk.security.internal.SecurityRepositoryFilter;
import io.crnk.security.internal.SecurityResourceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SecurityModule implements Module {

	protected static final String ALL_ROLE = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityModule.class);
	private Map<String, Map<String, ResourcePermission>> permissions;

	private ModuleContext context;

	private Supplier<Boolean> enabled = new Supplier<Boolean>() {

		@Override
		public Boolean get() {
			return Boolean.TRUE;
		}
	};

	private SecurityConfig config;

	// protected for CDI
	protected SecurityModule() {
	}

	protected SecurityModule(SecurityConfig config) {
		this.config = config;
	}

	public static SecurityModule newServerModule(SecurityConfig config) {
		return new SecurityModule(config);
	}

	public static SecurityModule newClientModule() {
		return new SecurityModule(null);
	}

	private static void configureRule(Map<String, Map<String, ResourcePermission>> newPermissions, String resourceType,
									  String role, ResourcePermission permission) {
		Map<String, ResourcePermission> set = newPermissions.get(resourceType);
		if (set == null) {
			set = new HashMap<>();
			newPermissions.put(resourceType, set);
		}
		ResourcePermission existingPermissions = set.get(role);
		ResourcePermission newPermission = permission;
		if (existingPermissions != null) {
			newPermission = existingPermissions.or(permission);
		}
		set.put(role, newPermission);

		LOGGER.debug("configure rule for resourceType={} role={} permission={}", resourceType, role, permission);
	}

	private static ResourcePermission updateMissingPermissions(ResourcePermission missingPermission,
															   ResourcePermission grantedPermissions) {
		return missingPermission.and(missingPermission.xor(grantedPermissions));
	}

	/**
	 * @param enabled to only perform security checks when true.
	 */
	public void setEnabled(final boolean enabled) {
		setEnabled(() -> enabled);
	}

	/**
	 * @return true if enabled
	 */
	public boolean isEnabled() {
		boolean en = enabled.get();
		LOGGER.debug("enabled={}", en);
		return en;
	}

	/**
	 * @param enabled supplier to only perform security checks when true.
	 */
	public void setEnabled(Supplier<Boolean> enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getModuleName() {
		return "security";
	}

	protected void checkInit() {
		if (config != null && permissions == null) {
			reconfigure(config);
		}
	}

	/**
	 * Applies the new configuration to this module.
	 */
	public void reconfigure(SecurityConfig config) {
		this.config = config;

		LOGGER.debug("reconfiguring with {} rules", config.getRules().size());

		Map<String, Map<String, ResourcePermission>> newPermissions = new HashMap<>();
		for (SecurityRule rule : config.getRules()) {
			String resourceType = rule.getResourceType();
			if (resourceType == null) {
				Class<?> resourceClass = rule.getResourceClass();
				if (resourceClass != null) {
					resourceType = toType(resourceClass);
				}
			}

			if (resourceType == null) {
				Collection<RegistryEntry> entries = context.getResourceRegistry().getResources();
				for (RegistryEntry entry : entries) {
					String entryResourceType = entry.getResourceInformation().getResourceType();
					configureRule(newPermissions, entryResourceType, rule.getRole(), rule.getPermission());
				}
			} else {
				ResourceRegistry resourceRegistry = context.getResourceRegistry();
				RegistryEntry entry = resourceRegistry.getEntry(resourceType);
				if (entry == null) {
					throw new RepositoryNotFoundException(resourceType);
				}
				configureRule(newPermissions, resourceType, rule.getRole(), rule.getPermission());
			}
		}
		this.permissions = newPermissions;
	}

	public SecurityConfig getConfig() {
		return config;
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.context = context;
		context.addRepositoryFilter(new SecurityRepositoryFilter(this));
		context.addResourceFilter(new SecurityResourceFilter(this));
	}

	/**
	 * @param resourceClass to check the permissions for
	 * @param permission    the required permissions.
	 * @return true if the requested permissions are satisfied for the given resourceClass.
	 */
	public boolean isAllowed(Class<?> resourceClass, ResourcePermission permission) {
		String resourceType = toType(resourceClass);
		return isAllowed(resourceType, permission);
	}

	/**
	 * @param resourceType to check the permissions for
	 * @param permission   the required permissions.
	 * @return true if the requested permissions are satisfied for the given resourceType.
	 */
	public boolean isAllowed(String resourceType, ResourcePermission permission) {
		if (!isEnabled()) {
			return true;
		}
		checkInit();
		Map<String, ResourcePermission> map = permissions.get(resourceType);
		ResourcePermission missingPermission = permission;
		if (map != null) {
			for (Entry<String, ResourcePermission> entry : map.entrySet()) {
				String role = entry.getKey();
				ResourcePermission intersection = entry.getValue().and(permission);
				boolean hasMorePermissions = !intersection.isEmpty();
				if (hasMorePermissions && isUserInRole(role)) {
					missingPermission = updateMissingPermissions(missingPermission, intersection);
					if (missingPermission.isEmpty()) {
						break;
					}
				}
			}
		}

		boolean allowed = missingPermission.isEmpty();
		LOGGER.debug("isAllowed returns {} for permission {} due to missing {}", allowed, permission, missingPermission);
		return allowed;
	}

	/**
	 * @param resourceClass to get the permissions for
	 * @return ResourcePermission for the given resource for the current user.
	 */
	public ResourcePermission getResourcePermission(Class<?> resourceClass) {
		String resourceType = toType(resourceClass);
		return getResourcePermission(resourceType);
	}

	/**
	 * @param resourceType to get the permissions for
	 * @return ResourcePermission for the given resource for the current user.
	 */
	public ResourcePermission getResourcePermission(String resourceType) {
		checkInit();
		if (!isEnabled()) {
			return ResourcePermission.ALL;
		}
		Map<String, ResourcePermission> map = permissions.get(resourceType);
		ResourcePermission result = ResourcePermission.EMPTY;
		if (map != null) {
			for (Entry<String, ResourcePermission> entry : map.entrySet()) {
				String role = entry.getKey();
				if (isUserInRole(role)) {
					result = result.or(entry.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * Checks whether the current user posses the provided role
	 *
	 * @param role to check
	 * @return true if in this role
	 */
	public boolean isUserInRole(String role) {
		if (!isEnabled()) {
			throw new IllegalStateException("security module is disabled");
		}
		checkInit();
		SecurityProvider securityProvider = context.getSecurityProvider();
		boolean contained = role == ALL_ROLE || securityProvider.isUserInRole(role);
		LOGGER.debug("isUserInRole returns {} for role {}", contained, role);
		return contained;
	}

	private <T> String toType(Class<T> resourceClass) {
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry entry = resourceRegistry.getEntryForClass(resourceClass);
		if (entry == null) {
			throw new RepositoryNotFoundException(resourceClass);
		}
		ResourceInformation resourceInformation = entry.getResourceInformation();
		return resourceInformation.getResourceType();
	}

}
