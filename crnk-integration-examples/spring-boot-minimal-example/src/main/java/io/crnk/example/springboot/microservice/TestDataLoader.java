package io.crnk.example.springboot.microservice;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestDataLoader {

	@Autowired
	private ProjectRepositoryImpl projectRepository;

	@PostConstruct
	public void setup() {
		projectRepository.save(new Project(121L, "Great Project"));
		projectRepository.save(new Project(122L, "Crnk Project"));
		projectRepository.save(new Project(123L, "Some Project"));
		projectRepository.save(new Project(124L, "JSON API Project"));
	}
}
