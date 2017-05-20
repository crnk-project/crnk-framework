package io.crnk.spring.domain.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.spring.domain.model.Project;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProjectRepository extends ResourceRepositoryBase<Project, String> {

	private Map<Long, Project> projects = new HashMap<>();

	public ProjectRepository() {
		super(Project.class);
		List<String> interests = new ArrayList<>();
		interests.add("coding");
		interests.add("art");
		save(new Project(1L, "Project A"));
		save(new Project(2L, "Project B"));
		save(new Project(3L, "Project C"));
	}

	@Override
	public synchronized void delete(String id) {
		projects.remove(id);
	}

	@Override
	public synchronized <S extends Project> S save(S project) {
		projects.put(project.getId(), project);
		return project;
	}

	@Override
	public synchronized ResourceList<Project> findAll(QuerySpec querySpec) {
		return querySpec.apply(projects.values());
	}
}
