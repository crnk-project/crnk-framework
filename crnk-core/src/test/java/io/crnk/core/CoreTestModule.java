package io.crnk.core;

import io.crnk.core.mock.repository.ComplexPojoRepository;
import io.crnk.core.mock.repository.CustomPagingPojoRepository;
import io.crnk.core.mock.repository.FancyProjectRepository;
import io.crnk.core.mock.repository.HierarchicalTaskRelationshipRepository;
import io.crnk.core.mock.repository.HierarchicalTaskRepository;
import io.crnk.core.mock.repository.LazyTaskRepository;
import io.crnk.core.mock.repository.PojoRepository;
import io.crnk.core.mock.repository.ProjectPatchStrategyRepository;
import io.crnk.core.mock.repository.ProjectPolymorphicRepository;
import io.crnk.core.mock.repository.ProjectRepository;
import io.crnk.core.mock.repository.ProjectToTaskRepository;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.mock.repository.RelationshipBehaviorTestRepository;
import io.crnk.core.mock.repository.ScheduleRepositoryImpl;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.mock.repository.TaskWithLookupRepository;
import io.crnk.core.mock.repository.TaskWithLookupToProjectRepository;
import io.crnk.core.mock.repository.ThingRepository;
import io.crnk.core.mock.repository.UserRepository;
import io.crnk.core.mock.repository.UserToProjectRepository;
import io.crnk.core.mock.repository.UserToTaskRepository;
import io.crnk.core.module.Module;

public class CoreTestModule implements Module {

    @Override
    public String getModuleName() {
        return "test";
    }

    @Override
    public void setupModule(ModuleContext context) {
        context.addRepository(new ProjectRepository());
        context.addRepository(new ProjectPatchStrategyRepository());
        context.addRepository(new ProjectToTaskRepository());
        context.addRepository(new ProjectPolymorphicRepository());
        context.addRepository(new ScheduleRepositoryImpl());
        context.addRepository(new ThingRepository());
        context.addRepository(new TaskRepository());
        context.addRepository(new TaskToProjectRepository());
        context.addRepository(new TaskWithLookupRepository());
        context.addRepository(new TaskWithLookupToProjectRepository());
        context.addRepository(new UserRepository());
        context.addRepository(new UserToProjectRepository());
        context.addRepository(new UserToTaskRepository());
        context.addRepository(new CustomPagingPojoRepository());
        context.addRepository(new LazyTaskRepository());
        context.addRepository(new RelationIdTestRepository());
        context.addRepository(new HierarchicalTaskRepository());
        context.addRepository(new HierarchicalTaskRelationshipRepository());
        context.addRepository(new RelationshipBehaviorTestRepository());
        context.addRepository(new ComplexPojoRepository());
        context.addRepository(new PojoRepository());
        context.addRepository(new FancyProjectRepository());
    }
}
