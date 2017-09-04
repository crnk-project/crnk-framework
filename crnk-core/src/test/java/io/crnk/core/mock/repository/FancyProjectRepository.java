package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.FancyProject;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashMap;
import java.util.Map;

public class FancyProjectRepository extends ResourceRepositoryBase<FancyProject, Long> {

	private static Map<Long, FancyProject> projects = new HashMap<>();

	public FancyProjectRepository() {
		super(FancyProject.class);
	}

	public static void clear() {
		projects.clear();
	}

	@Override
	public ResourceList<FancyProject> findAll(QuerySpec querySpec) {
		return querySpec.apply(projects.values());
	}

	@Override
	public <S extends FancyProject> S save(S entity) {
		projects.put(entity.getId(), entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		projects.remove(id);
	}
}