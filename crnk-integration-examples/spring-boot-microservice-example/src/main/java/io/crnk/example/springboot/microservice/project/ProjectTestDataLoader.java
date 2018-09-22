package io.crnk.example.springboot.microservice.project;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectTestDataLoader {

	@Autowired
	private ProjectRepository projectRepository;

	@PostConstruct
	public void setup() {
		projectRepository.save(new Project(121L, "Great Project"));
		projectRepository.save(new Project(122L, "Crnk Project"));
		projectRepository.save(new Project(123L, "Some Project"));
		projectRepository.save(new Project(124L, "JSON API Project"));
	}
}
