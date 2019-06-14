package io.crnk.example.springboot.proxied.microservice.task;

import io.crnk.core.repository.InMemoryResourceRepository;
import org.springframework.stereotype.Component;

@Component
public class TaskRepository extends InMemoryResourceRepository<Task, Long> {

	public TaskRepository() {
		super(Task.class);
	}
}
