package io.crnk.example.springboot.proxied.microservice.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ProjectTestDataLoader {

	@Autowired
	private ProjectRepository projectRepository;

	@PostConstruct
	public void setup() {
		projectRepository.save(new Project(121L, "Great Project", "P1", "Mike"));
		projectRepository.save(new Project(122L, "Crnk Project", "P2", "Nick"));
		projectRepository.save(new Project(123L, "Some Project","P3", "Tom"));
		projectRepository.save(new Project(124L, "JSON API Project", "P24", "John"));
	}
}
