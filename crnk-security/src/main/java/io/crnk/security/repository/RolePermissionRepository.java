package io.crnk.security.repository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.security.SecurityProviderContext;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.security.SecurityModule;

public class RolePermissionRepository extends ResourceRepositoryBase<RolePermission, UUID> implements ResourceRegistryAware {

	private final SecurityModule module;

	private ResourceRegistry resourceRegistry;

	public RolePermissionRepository(SecurityModule module) {
		super(RolePermission.class);
		this.module = module;
	}

	@Override
	public ResourceList<RolePermission> findAll(QuerySpec querySpec) {
		Set<String> roles = module.getConfig().getRules()
				.stream()
				.filter(it -> it.getRole() != null)
				.map(it -> it.getRole())
				.collect(Collectors.toSet());

		Set<RolePermission> permissions = new HashSet<>();

		resourceRegistry.getEntries().stream()
				.map(entry -> entry.getResourceInformation())
				.forEach(resourceInformation -> {
					String resourceType = resourceInformation.getResourceType();
					QuerySpec filterQuerySpec = module.getDataRoomMatcher().filter(new QuerySpec(resourceInformation), HttpMethod.GET, new SecurityProvider() {
						@Override
						public boolean isUserInRole(String role, SecurityProviderContext context) {
							return role.equals(role);
						}

						@Override
						public boolean isAuthenticated() {
							return true;
						}
					});
					for (String role : roles) {
						RolePermission rolePermission = new RolePermission();
						rolePermission.setRole(role);
						rolePermission.setId(resourceType + "_" + role);
						rolePermission.setResourceType(resourceType);
						rolePermission.setPermission(module.getRolePermissions(resourceType, role));
						rolePermission.setDataRoomFilter(FilterSpec.and(filterQuerySpec.getFilters()));
						permissions.add(rolePermission);
					}
				});
		return querySpec.apply(permissions);
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		super.setResourceRegistry(resourceRegistry);
		this.resourceRegistry = resourceRegistry;
	}
}
