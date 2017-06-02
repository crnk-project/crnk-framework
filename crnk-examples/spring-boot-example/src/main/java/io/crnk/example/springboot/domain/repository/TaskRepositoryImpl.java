/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.crnk.example.springboot.domain.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.example.springboot.domain.model.Task;
import io.crnk.legacy.repository.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@JsonApiResourceRepository(Task.class)
@Validated
public class TaskRepositoryImpl {
	private static final Map<Long, Task> REPOSITORY = new ConcurrentHashMap<>();
	private static final AtomicLong ID_GENERATOR = new AtomicLong(4);

	private final ProjectRepositoryImpl projectRepository;

	@Autowired
	@Lazy
	public TaskRepositoryImpl(ProjectRepositoryImpl projectRepository) {
		this.projectRepository = projectRepository;
		Task task = new Task(1L, "Create tasks");
		task.setProjectId(123L);
		save(task);
		task = new Task(2L, "Make coffee");
		task.setProjectId(123L);
		save(task);
		task = new Task(3L, "Do things");
		task.setProjectId(123L);
		save(task);
	}

	@JsonApiSave
	public <S extends Task> S save(@Valid S entity) {
		if (entity.getId() == null) {
			entity.setId(ID_GENERATOR.getAndIncrement());
		}
		REPOSITORY.put(entity.getId(), entity);
		return entity;
	}

	@JsonApiFindOne
	public Task findOne(Long taskId, QuerySpec requestParams) {
		Task task = REPOSITORY.get(taskId);
		if (task == null) {
			throw new ResourceNotFoundException("Project not found!");
		}
		if (task.getProject() == null) {
			task.setProject(projectRepository.findOne(task.getProjectId(), new QuerySpec(Task.class)));
		}
		return task;
	}

	@JsonApiFindAll
	public Iterable<Task> findAll(QuerySpec requestParams) {
		return REPOSITORY.values();
	}

	@JsonApiFindAllWithIds
	public Iterable<Task> findAll(Iterable<Long> taskIds, QuerySpec requestParams) {
		List<Task> foundTasks = new ArrayList<>();
		for (Map.Entry<Long, Task> entry : REPOSITORY.entrySet()) {
			for (Long id : taskIds) {
				if (id.equals(entry.getKey())) {
					foundTasks.add(entry.getValue());
				}
			}
		}
		return foundTasks;
	}

	@JsonApiDelete
	public void delete(Long taskId) {
		REPOSITORY.remove(taskId);
	}
}
