package io.crnk.gen.openapi.mock;

import io.crnk.core.module.Module;
import io.crnk.test.mock.TestExceptionMapper;
import io.crnk.test.mock.repository.TaskRepository;

public class SimpleTestModule implements Module {

	private SimpleTaskRepository tasks = new SimpleTaskRepository(SimpleTask.class);

	@Override
	public String getModuleName() {
		return "test";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepository(tasks);
		context.addExceptionMapper(new TestExceptionMapper());
	}

	public SimpleTaskRepository getTasks() {
		return tasks;
	}

	public static void clear() {
		TaskRepository.clear();
	}
}
