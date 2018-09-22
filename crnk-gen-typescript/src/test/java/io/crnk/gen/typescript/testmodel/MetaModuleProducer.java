package io.crnk.gen.typescript.testmodel;

import io.crnk.core.module.Module;
import io.crnk.core.module.SimpleModule;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.ProjectToTaskRepository;
import io.crnk.test.mock.repository.ScheduleRepositoryImpl;
import io.crnk.test.mock.repository.ScheduleToTaskRepository;
import io.crnk.test.mock.repository.TaskRepository;
import io.crnk.test.mock.repository.TaskSubtypeRepository;
import io.crnk.test.mock.repository.TaskToProjectRepository;
import io.crnk.test.mock.repository.TaskToScheduleRepo;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class MetaModuleProducer {

	@Produces
	@ApplicationScoped
	public MetaModule createMetaModule() {
		MetaModuleConfig metaConfig = new MetaModuleConfig();
		metaConfig.addMetaProvider(new ResourceMetaProvider());
		MetaModule metaModule = MetaModule.createServerModule(metaConfig);
		return metaModule;
	}

	@Produces
	@ApplicationScoped
	public Module createRepositoryModule() {
		SimpleModule module = new SimpleModule("mock");
		module.addRepository(new ScheduleRepositoryImpl());
		module.addRepository(new ProjectRepository());
		module.addRepository(new TaskRepository());
		module.addRepository(new ProjectToTaskRepository());
		module.addRepository(new ScheduleToTaskRepository());
		module.addRepository(new TaskSubtypeRepository());
		module.addRepository(new TaskToProjectRepository());
		module.addRepository(new TaskToScheduleRepo());
		return module;
	}

}
