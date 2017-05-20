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
package io.crnk.servlet.resource.repository;

import io.crnk.legacy.repository.annotations.*;
import io.crnk.servlet.resource.model.Task;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@JsonApiResourceRepository(Task.class)
public class TaskRepository {

	@JsonApiSave
	public <S extends Task> S save(S entity) {
		entity.setId(1L);
		return entity;
	}

	@JsonApiFindOne
	public Task findOne(Long aLong, HttpServletRequest httpServletRequest) {
		Task task = new Task(aLong, "Some task");
		return task;
	}

	@JsonApiFindAll
	public Iterable<Task> findAll() {
		return findAll(null);
	}

	@JsonApiFindAllWithIds
	public Iterable<Task> findAll(Iterable<Long> ids) {
		return Collections.singletonList(new Task(1L, "First task"));
	}

	@JsonApiDelete
	public void delete(Long aLong) {

	}
}

