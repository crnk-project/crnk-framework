package io.crnk.security.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.security.SecurityModule;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
                .filter(it -> it.getRole() != null)
                .map(it -> new Role(it.getRole()))
                .collect(Collectors.toSet());
        return querySpec.apply(resources);
    }
}
