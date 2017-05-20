package io.crnk.security.model;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.security.ResourcePermissionInformationImpl;

import java.util.HashMap;
import java.util.Map;


public class ProjectRepository extends ResourceRepositoryBase<Project, Long> {

	private static final Map<Long, Project> PROJECTS = new HashMap<>();

	public ProjectRepository() {
		super(Project.class);
	}

	public static void clear() {
		PROJECTS.clear();
	}

	@Override
	public <S extends Project> S save(S entity) {
		PROJECTS.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public ResourceList<Project> findAll(QuerySpec querySpec) {
		DefaultResourceList<Project> list = querySpec.apply(PROJECTS.values());
		list.setMeta(new ResourcePermissionInformationImpl());
		return list;
	}

	@Override
	public void delete(Long id) {
		PROJECTS.remove(id);
	}
}
