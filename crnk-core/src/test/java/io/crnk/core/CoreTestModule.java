package io.crnk.core;

import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.mock.repository.ThingRepository;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.repository.ProjectQuerySpecRepository;
import io.crnk.core.queryspec.repository.ProjectToTaskRelationshipRepository;
import io.crnk.core.queryspec.repository.ScheduleQuerySpecRepository;
import io.crnk.core.queryspec.repository.TaskQuerySpecRepository;
import io.crnk.core.queryspec.repository.TaskSubtypeRepository;
import io.crnk.core.queryspec.repository.TaskToProjectRelationshipRepository;
import io.crnk.core.queryspec.repository.TaskWithLookupQuerySpecRepository;

public class CoreTestModule implements Module {

	public static void clear() {
		TaskQuerySpecRepository.clear();
		TaskSubtypeRepository.clear();
		ProjectQuerySpecRepository.clear();
		ScheduleRepositoryImpl.clear();
	}

	@Override
	public String getModuleName() {
		return "test";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepository(new ProjectQuerySpecRepository());
		context.addRepository(new ProjectToTaskRelationshipRepository());
		context.addRepository(new ScheduleQuerySpecRepository());
		context.addRepository(new ThingRepository());
		context.addRepository(new TaskQuerySpecRepository());
		context.addRepository(new TaskToProjectRelationshipRepository());
		context.addRepository(new TaskWithLookupQuerySpecRepository());
		context.addRepository(new TaskSubtypeRepository());
	}
}
