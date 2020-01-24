package io.crnk.monitor.brave.mock.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.monitor.brave.mock.models.Project;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ProjectRepository extends ResourceRepositoryBase<Project, Long> {

	private static final AtomicLong ID_GENERATOR = new AtomicLong(124);

	private static Map<Long, Project> resources = new HashMap<>();

	public ProjectRepository() {
		super(Project.class);
		save(new Project(123L, "Great Project"));
	}

	public static void clear() {
		resources.clear();
	}

	@Override
	public synchronized void delete(Long id) {
		resources.remove(id);
	}

	@Override
	public synchronized <S extends Project> S save(S project) {
		if (project.getId() == null) {
			project.setId(ID_GENERATOR.getAndIncrement());
		}
		resources.put(project.getId(), project);
		return project;
	}

	@Override
	public synchronized ResourceList<Project> findAll(QuerySpec querySpec) {
		return querySpec.apply(resources.values());
	}
}
