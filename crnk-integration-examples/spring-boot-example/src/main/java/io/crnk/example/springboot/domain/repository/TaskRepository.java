package io.crnk.example.springboot.domain.repository;

import io.crnk.core.repository.ResourceRepository;
import io.crnk.example.springboot.domain.model.Task;

public interface TaskRepository extends ResourceRepository<Task, Long> {

}
