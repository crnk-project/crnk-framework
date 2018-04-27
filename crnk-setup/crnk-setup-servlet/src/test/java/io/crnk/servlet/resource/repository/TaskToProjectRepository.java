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

import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.servlet.resource.model.Project;
import io.crnk.servlet.resource.model.Task;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

	@Override
	public void setRelation(Task task, Long projectId, String fieldName) {

	}

	@Override
	public void setRelations(Task task, Iterable<Long> projectId, String fieldName) {

	}

	@Override
	public void addRelations(Task source, Iterable<Long> targetIds, String fieldName) {
	}

	@Override
	public void removeRelations(Task source, Iterable<Long> targetIds, String fieldName) {
	}

	@Override
	public Project findOneTarget(Long sourceId, String fieldName, QueryParams requestParams) {
		return null;
	}

	@Override
	public Iterable<Project> findManyTargets(Long sourceId, String fieldName, QueryParams requestParams) {
		return null;
	}
}
