package io.crnk.example.jersey.domain.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.example.jersey.domain.model.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ProjectRepositoryImpl extends ResourceRepositoryBase<Project, Long> implements ProjectRepository {

	private static final AtomicLong ID_GENERATOR = new AtomicLong(124);

	private Map<Long, Project> projects = new HashMap<>();

	public ProjectRepositoryImpl() {
		super(Project.class);
		List<String> interests = new ArrayList<>();
		interests.add("coding");
		interests.add("art");
		save(new Project(121L, "Great Project"));
		save(new Project(122L, "Crnk Project"));
		save(new Project(123L, "Some Project"));
		save(new Project(124L, "JSON API Project"));
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
