package io.crnk.example.springboot.microservice.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class TaskTestDataLoader {

	@Autowired
	private TaskRepository taskRepository;

	@PostConstruct
	public void setup() {
		taskRepository.save(new Task(221L, "Great Task", 121L));
		taskRepository.save(new Task(222L, "Crnk Task", 121L));
		taskRepository.save(new Task(223L, "Some Task", 121L));
		taskRepository.save(new Task(224L, "JSON API Task", 121L));
	}
}
