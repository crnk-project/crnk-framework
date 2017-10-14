package io.crnk.test.mock;

import io.crnk.core.module.Module;
import io.crnk.test.mock.repository.*;

public class TestModule implements Module {

	@Override
	public String getModuleName() {
		return "test";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepository(new TaskRepository());
		context.addRepository(new ProjectRepository());
		context.addRepository(new ScheduleRepositoryImpl());
		context.addRepository(new TaskSubtypeRepository());
		context.addRepository(new ProjectToTaskRepository());
		context.addRepository(new ScheduleToTaskRepository());
		context.addRepository(new TaskToProjectRepository());
		context.addRepository(new TaskToScheduleRepo());
		context.addRepository(new PrimitiveAttributeRepository());
		context.addExceptionMapper(new TestExceptionMapper());
	}
}
