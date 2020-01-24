package io.crnk.example.springboot.domain.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.example.springboot.domain.model.Project;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ProjectRepositoryImpl extends ResourceRepositoryBase<Project, Long> implements ProjectRepository {

	private static final AtomicLong ID_GENERATOR = new AtomicLong(124);

	private Map<Long, Project> projects = new HashMap<>();

	public ProjectRepositoryImpl() {
		super(Project.class);
	}

	@Override
	public synchronized void delete(Long id) {
		projects.remove(id);
	}

	@Override
	public synchronized <S extends Project> S save(S project) {
		if (project.getId() == null) {
			project.setId(ID_GENERATOR.getAndIncrement());
		}
		projects.put(project.getId(), project);
		return project;
	}

	@Override
	public synchronized ProjectList findAll(QuerySpec querySpec) {
		ProjectList list = new ProjectList();
		list.setMeta(new ProjectListMeta());
		list.setLinks(new ProjectListLinks());
		querySpec.apply(projects.values(), list);
		return list;
	}

}
