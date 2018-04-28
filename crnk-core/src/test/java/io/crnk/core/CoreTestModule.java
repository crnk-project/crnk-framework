package io.crnk.core;

import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.repository.*;

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
		context.addRepository(new TaskQuerySpecRepository());
		context.addRepository(new TaskToProjectRelationshipRepository());
		context.addRepository(new TaskWithLookupQuerySpecRepository());
		context.addRepository(new TaskSubtypeRepository());
	}
}
