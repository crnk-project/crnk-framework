package io.crnk.reactive;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.SimpleModule;
import io.crnk.reactive.internal.MonoResultFactory;
import io.crnk.reactive.model.ReactiveProject;
import io.crnk.reactive.model.ReactiveProjectToTasksRepository;
import io.crnk.reactive.model.ReactiveTask;
import io.crnk.reactive.model.ReactiveTaskToProjectRepository;
import io.crnk.reactive.model.InMemoryReactiveResourceRepository;
import org.junit.Before;
import org.mockito.Mockito;

public abstract class ReactiveTestBase {

	protected CrnkBoot boot;

	protected InMemoryReactiveResourceRepository<ReactiveTask, Long> taskRepository;

	protected InMemoryReactiveResourceRepository<ReactiveProject, Long> projectRepository;

	protected ReactiveTaskToProjectRepository taskToProject;

	protected ReactiveProjectToTasksRepository projectToTasks;

	protected QueryContext queryContext = new QueryContext();

	protected HttpRequestContextBaseAdapter requestContext;

	protected HttpRequestContextBase requestContextBase;

	@Before
	public void setup() {
		taskRepository = new InMemoryReactiveResourceRepository(ReactiveTask.class);
		projectRepository = new InMemoryReactiveResourceRepository(ReactiveProject.class);
		taskToProject = new ReactiveTaskToProjectRepository(projectRepository.getMap());
		projectToTasks = new ReactiveProjectToTasksRepository(taskRepository.getMap());

		SimpleModule testModule = new SimpleModule("test");
		testModule.addRepository(taskRepository);
		testModule.addRepository(projectRepository);
		testModule.addRepository(taskToProject);
		testModule.addRepository(projectToTasks);

		boot = new CrnkBoot();
		boot.addModule(new ReactiveModule());
		boot.addModule(testModule);
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost:8080"));
		setup(boot);
		boot.boot();

		requestContextBase = Mockito.mock(HttpRequestContextBase.class);
		requestContext = new HttpRequestContextBaseAdapter(requestContextBase);
		queryContext = requestContext.getQueryContext();
		queryContext.setBaseUrl(boot.getServiceUrlProvider().getUrl());

		HttpRequestContextProvider requestContextProvider = boot.getModuleRegistry().getHttpRequestContextProvider();
		requestContextProvider.onRequestStarted(requestContext);
	}

	protected void setup(CrnkBoot boot) {
	}

	protected ReactiveTask createTask(int id) {
		ReactiveTask task = new ReactiveTask();
		task.setId((long) id);
		task.setName("task" + id);
		return task;
	}

	protected ReactiveProject createProject(int id) {
		ReactiveProject project = new ReactiveProject();
		project.setId((long) id);
		project.setName("task" + id);
		return project;
	}

}
