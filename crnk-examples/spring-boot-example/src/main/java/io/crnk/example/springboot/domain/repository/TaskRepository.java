package io.crnk.example.springboot.domain.repository;

import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.example.springboot.domain.model.Task;

public interface TaskRepository extends ResourceRepositoryV2<Task, Long> {

}
