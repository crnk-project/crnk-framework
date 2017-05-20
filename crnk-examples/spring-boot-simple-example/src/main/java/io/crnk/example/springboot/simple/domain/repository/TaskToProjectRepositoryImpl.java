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
package io.crnk.example.springboot.simple.domain.repository;

import io.crnk.core.repository.RelationshipRepositoryBase;
import io.crnk.example.springboot.simple.domain.model.Project;
import io.crnk.example.springboot.simple.domain.model.Task;
import org.springframework.stereotype.Component;

/**
 * Example based on RelationshipRepositoryBase which by default accesses the repositories on both sides.
 */
@Component
public class TaskToProjectRepositoryImpl extends RelationshipRepositoryBase<Task, Long, Project, Long> {

	public TaskToProjectRepositoryImpl() {
		super(Task.class, Project.class);
	}
}
