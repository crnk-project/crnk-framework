package io.crnk.test.mock.reactive;

import io.crnk.core.module.Module;
import io.crnk.test.mock.TestExceptionMapper;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.RelationIdTestResource;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.*;

public class ReactiveTestModule implements Module {

	private InMemoryReactiveResourceRepository<Task, Object> taskRepository = new InMemoryReactiveResourceRepository<>(Task.class);

	private InMemoryReactiveResourceRepository<Project, Long> projectRepository = new ReactiveProjectRepository();
	private InMemoryReactiveResourceRepository<Schedule, Long> scheduleRepository = new InMemoryReactiveResourceRepository<>(Schedule.class);
	private InMemoryReactiveResourceRepository<RelationIdTestResource, Long> relationIdTestRepository = new InMemoryReactiveResourceRepository<>(RelationIdTestResource.class);

	@Override
	public String getModuleName() {
		return "test";
	}

	@Override
	public void setupModule(ModuleContext context) {

		context.addRepository(taskRepository);
		context.addRepository(projectRepository);
		context.addRepository(scheduleRepository);
		context.addRepository(relationIdTestRepository);

		context.addRepository(new TaskSubtypeRepository());

		context.addRepository(new ProjectToTaskRepository());
		context.addRepository(new ScheduleToTaskRepository());
		context.addRepository(new TaskToProjectRepository());
		context.addRepository(new TaskToScheduleRepo());
		context.addRepository(new PrimitiveAttributeRepository());

		context.addExceptionMapper(new TestExceptionMapper());
	}

	public InMemoryReactiveResourceRepository<Task, Object> getTaskRepository() {
		return taskRepository;
	}

	public void clear() {
		TestModule.clear();
		taskRepository.clear();
		projectRepository.clear();
		scheduleRepository.clear();
		relationIdTestRepository.clear();
	}

	public InMemoryReactiveResourceRepository<Schedule, Long> getScheduleRepository() {
		return scheduleRepository;
	}

	public InMemoryReactiveResourceRepository<Project, Long> getProjectRepository() {
		return projectRepository;
	}

	public InMemoryReactiveResourceRepository<RelationIdTestResource, Long> getRelationIdTestRepository() {
		return relationIdTestRepository;
	}
}
