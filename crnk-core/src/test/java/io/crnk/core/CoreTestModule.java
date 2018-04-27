package io.crnk.core;

import io.crnk.core.module.Module;
import io.crnk.core.queryspec.repository.*;

public class CoreTestModule implements Module {
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
		context.addRepository(new TaskWithPagingBehaviorQuerySpecRepository());
		context.addRepository(new TaskWithPagingBehaviorToProjectRelationshipRepository());
	}
}
