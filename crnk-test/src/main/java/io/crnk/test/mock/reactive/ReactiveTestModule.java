package io.crnk.test.mock.reactive;

import io.crnk.core.module.Module;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.TestExceptionMapper;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.TestNamingStrategy;
import io.crnk.test.mock.models.HistoricTask;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.RelationIdTestResource;
import io.crnk.test.mock.models.RelocatedTask;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.PrimitiveAttributeRepository;
import io.crnk.test.mock.repository.ProjectToTaskRepository;
import io.crnk.test.mock.repository.TaskSubtypeRepository;
import io.crnk.test.mock.repository.TaskToProjectRepository;

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
		context.addRepository(new ReactiveReadOnlyTaskRepository());

		context.addRepository(new TaskSubtypeRepository());
		context.addRepository(new InMemoryResourceRepository(HistoricTask.class));
		context.addRepository(new InMemoryResourceRepository(RelocatedTask.class));

		context.addRepository(new ProjectToTaskRepository());
		context.addRepository(new TaskToProjectRepository());
		context.addRepository(new PrimitiveAttributeRepository());

		context.addNamingStrategy(new TestNamingStrategy());
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
