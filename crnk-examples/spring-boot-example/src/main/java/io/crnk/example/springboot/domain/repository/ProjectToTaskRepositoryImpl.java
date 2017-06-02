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

import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.example.springboot.domain.model.Project;
import io.crnk.example.springboot.domain.model.Task;
import io.crnk.legacy.repository.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Manually-written, annotation-based relationship repository example.
 */
@JsonApiRelationshipRepository(source = Project.class, target = Task.class)
@Component
public class ProjectToTaskRepositoryImpl {

	private final ProjectRepositoryImpl projectRepository;
	private final TaskRepositoryImpl taskRepository;

	@Autowired
	public ProjectToTaskRepositoryImpl(ProjectRepositoryImpl projectRepository, TaskRepositoryImpl taskRepository) {
		this.projectRepository = projectRepository;
		this.taskRepository = taskRepository;
	}

	@JsonApiSetRelation
	public void setRelation(Project project, Long taskId, String fieldName) {
		Task task = taskRepository.findOne(taskId, null);
		try {
			PropertyUtils.setProperty(project, fieldName, task);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		projectRepository.save(project);
	}

	@JsonApiSetRelations
	public void setRelations(Project project, Iterable<Long> taskIds, String fieldName) {
		Iterable<Task> tasks = taskRepository.findAll(taskIds, null);
		try {
			PropertyUtils.setProperty(project, fieldName, tasks);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		projectRepository.save(project);
	}

	@JsonApiAddRelations
	public void addRelations(Project project, Iterable<Long> taskIds, String fieldName) {
		List<Task> newTaskList = new LinkedList<>();
		Iterable<Task> tasksToAdd = taskRepository.findAll(taskIds, null);
		for (Task task : tasksToAdd) {
			newTaskList.add(task);
		}
		try {
			if (PropertyUtils.getProperty(project, fieldName) != null) {
				Iterable<Task> tasks = (Iterable<Task>) PropertyUtils.getProperty(project, fieldName);
				for (Task task : tasks) {
					newTaskList.add(task);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			PropertyUtils.setProperty(project, fieldName, newTaskList);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		projectRepository.save(project);

	}

	@JsonApiRemoveRelations
	public void removeRelations(Project project, Iterable<Long> taskIds, String fieldName) {
		try {
			if (PropertyUtils.getProperty(project, fieldName) != null) {
				Iterable<Task> tasks = (Iterable<Task>) PropertyUtils.getProperty(project, fieldName);
				Iterator<Task> iterator = tasks.iterator();
				while (iterator.hasNext()) {
					for (Long taskIdToRemove : taskIds) {
						if (iterator.next().getId().equals(taskIdToRemove)) {
							iterator.remove();
							break;
						}
					}
				}
				List<Task> newTaskList = new LinkedList<>();
				for (Task task : tasks) {
					newTaskList.add(task);
				}

				PropertyUtils.setProperty(project, fieldName, newTaskList);
				projectRepository.save(project);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@JsonApiFindOneTarget
	public Task findOneTarget(Long projectId, String fieldName, QuerySpec requestParams) {
		Project project = projectRepository.findOne(projectId, requestParams);
		try {
			return (Task) PropertyUtils.getProperty(project, fieldName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@JsonApiFindManyTargets
	public Iterable<Task> findManyTargets(Long projectId, String fieldName, QuerySpec requestParams) {
		Project project = projectRepository.findOne(projectId, requestParams);
		try {
			return (Iterable<Task>) PropertyUtils.getProperty(project, fieldName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
