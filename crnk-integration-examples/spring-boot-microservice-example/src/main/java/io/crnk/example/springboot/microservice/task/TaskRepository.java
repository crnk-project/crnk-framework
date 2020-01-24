package io.crnk.example.springboot.microservice.task;

import io.crnk.core.repository.InMemoryResourceRepository;
import org.springframework.stereotype.Component;

@Component
public class TaskRepository extends InMemoryResourceRepository<Task, Long> {

	public TaskRepository() {
		super(Task.class);
	}
}
