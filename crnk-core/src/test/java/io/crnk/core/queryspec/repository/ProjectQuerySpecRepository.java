package io.crnk.core.queryspec.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ProjectQuerySpecRepository extends ResourceRepositoryBase<Project, Long> {

	private static Set<Project> projects = new HashSet<>();

	public ProjectQuerySpecRepository() {
		super(Project.class);
	}

	public static void clear() {
		projects.clear();
	}

	@Override
	public ResourceList<Project> findAll(QuerySpec querySpec) {
		return querySpec.apply(projects);
	}

	@Override
	public <S extends Project> S save(S entity) {
		delete(entity.getId()); // replace current one

		// maintain bidirectional mapping, not perfect, should be done in the resources, but serves its purpose her.
		for (Task task : entity.getTasks()) {
			task.setProject(entity);
		}

		projects.add(entity);
		return entity;
	}

	@Override
	public void delete(Long id) {
		Iterator<Project> iterator = projects.iterator();
		while (iterator.hasNext()) {
			Project next = iterator.next();
			if (next.getId().equals(id)) {
				iterator.remove();
			}
		}
	}
}