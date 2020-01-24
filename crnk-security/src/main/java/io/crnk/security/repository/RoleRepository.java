package io.crnk.security.repository;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.security.SecurityModule;

public class RoleRepository extends ResourceRepositoryBase<Role, UUID> {

    private final SecurityModule module;

    public RoleRepository(SecurityModule module) {
        super(Role.class);
        this.module = module;
    }

    @Override
    public ResourceList<Role> findAll(QuerySpec querySpec) {
        Set<Role> resources = module.getConfig().getRules()
                .stream()
				.map(it -> it.getRole())
				.filter(roleName -> roleName != null)
				.distinct()
                .map(roleName -> new Role(roleName))
                .collect(Collectors.toSet());
        return querySpec.apply(resources);
    }
}
