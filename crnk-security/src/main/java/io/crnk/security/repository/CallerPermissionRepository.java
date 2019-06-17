package io.crnk.security.repository;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.security.SecurityModule;

public class CallerPermissionRepository extends ResourceRepositoryBase<CallerPermission, UUID> implements ResourceRegistryAware {

	private final SecurityModule module;

	private ResourceRegistry resourceRegistry;

	public CallerPermissionRepository(SecurityModule module) {
		super(CallerPermission.class);
		this.module = module;
	}

	@Override
	public ResourceList<CallerPermission> findAll(QuerySpec querySpec) {
		Set<CallerPermission> permissions = resourceRegistry.getEntries().stream()
				.map(entry -> entry.getResourceInformation())
				.map(resourceInformation -> {
					String resourceType = resourceInformation.getResourceType();
					QuerySpec filterQuerySpec = module.getDataRoomMatcher().filter(new QuerySpec(resourceInformation), HttpMethod.GET, module.getCallerSecurityProvider());
					CallerPermission callerPermission = new CallerPermission();
					callerPermission.setResourceType(resourceType);
					callerPermission.setPermission(module.getCallerPermissions(resourceType));
					callerPermission.setDataRoomFilter(FilterSpec.and(filterQuerySpec.getFilters()));
					return callerPermission;
				}).collect(Collectors.toSet());
		return querySpec.apply(permissions);
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		super.setResourceRegistry(resourceRegistry);
		this.resourceRegistry = resourceRegistry;
	}
}
