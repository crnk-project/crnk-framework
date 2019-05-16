package io.crnk.core.queryspec.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.LinksRepository;
import io.crnk.core.repository.MetaRepository;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.MetaInformation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TaskQuerySpecRepository extends ResourceRepositoryBase<Task, Long>
		implements MetaRepository<Task>, LinksRepository<Task> {

	private static Set<Task> tasks = new HashSet<>();

	public TaskQuerySpecRepository() {
		super(Task.class);
	}

	public static void clear() {
		tasks.clear();
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec querySpec) {
		return querySpec.apply(tasks);
	}

	@Override
	public <S extends Task> S save(S entity) {
		delete(entity.getId()); // replace current one

		// maintain bidirectional mapping, not perfect, should be done in the resources, but serves its purpose her.
		Project project = entity.getProject();
		if (project != null && !project.getTasks().contains(entity)) {
			project.getTasks().add(entity);
		}

		tasks.add(entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		Iterator<Task> iterator = tasks.iterator();
		while (iterator.hasNext()) {
			Task next = iterator.next();
			if (next.getId().equals(id)) {
				iterator.remove();
			}
		}
	}

	@Override
	public LinksInformation getLinksInformation(Collection<Task> resources, QuerySpec queryParams) {
		return new LinksInformation() {

			public String name = "value";
		};
	}

	@Override
	public MetaInformation getMetaInformation(Collection<Task> resources, QuerySpec queryParams) {
		return new MetaInformation() {

			public String name = "value";
		};
	}
}