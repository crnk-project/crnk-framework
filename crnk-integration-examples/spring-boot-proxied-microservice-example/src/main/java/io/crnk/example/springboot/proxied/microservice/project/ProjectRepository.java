package io.crnk.example.springboot.proxied.microservice.project;

import io.crnk.core.repository.InMemoryResourceRepository;
import org.springframework.stereotype.Component;

@Component
public class ProjectRepository extends InMemoryResourceRepository<Project, Long> {

	public ProjectRepository() {
		super(Project.class);
	}
}
