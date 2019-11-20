package io.crnk.test.mock;

import io.crnk.core.module.Module;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.BulkTask;
import io.crnk.test.mock.models.NoAccessTask;
import io.crnk.test.mock.models.RelocatedTask;
import io.crnk.test.mock.models.ResourceIdentifierBasedRelationshipResource;
import io.crnk.test.mock.models.VersionedTask;
import io.crnk.test.mock.repository.BulkInMemoryRepository;
import io.crnk.test.mock.repository.HistoricTaskRepository;
import io.crnk.test.mock.repository.PrimitiveAttributeRepository;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.ProjectToTaskRepository;
import io.crnk.test.mock.repository.ReadOnlyTaskRepository;
import io.crnk.test.mock.repository.RelationIdTestRepository;
import io.crnk.test.mock.repository.RenamedIdRepository;
import io.crnk.test.mock.repository.ScheduleRepositoryImpl;
import io.crnk.test.mock.repository.ScheduleStatusRepositoryImpl;
import io.crnk.test.mock.repository.TaskRepository;
import io.crnk.test.mock.repository.TaskSubtypeRepository;
import io.crnk.test.mock.repository.TaskToProjectRepository;
import io.crnk.test.mock.repository.nested.ManyNestedRepository;
import io.crnk.test.mock.repository.nested.NestedManyRelationshipRepository;
import io.crnk.test.mock.repository.nested.NestedOneRelationshipRepository;
import io.crnk.test.mock.repository.nested.OneNestedRepository;
import io.crnk.test.mock.repository.nested.ParentRepository;
import io.crnk.test.mock.repository.nested.RelatedRepository;

public class TestModule implements Module {

    private static ManyNestedRepository manyNestedRepository = new ManyNestedRepository();

    private static OneNestedRepository oneNestedRepository = new OneNestedRepository();

    private static RelatedRepository relatedRepository = new RelatedRepository();

    private static ParentRepository parentRepository = new ParentRepository();

    private static NestedManyRelationshipRepository nestedManyRelationshipRepository = new NestedManyRelationshipRepository();

    private static NestedOneRelationshipRepository nestedOneRelationshipRepository = new NestedOneRelationshipRepository();

    private ProjectRepository projects = new ProjectRepository();

    private TaskRepository tasks = new TaskRepository();

    private boolean extended = true;

    private BulkInMemoryRepository<BulkTask, Object> bulkTasks = new BulkInMemoryRepository<>(BulkTask.class);

    @Override
    public String getModuleName() {
        return "test";
    }

    @Override
    public void setupModule(ModuleContext context) {
        context.addRepository(tasks);
        context.addRepository(projects);
        context.addRepository(new ScheduleRepositoryImpl());
        context.addRepository(new TaskSubtypeRepository());
        context.addRepository(new ProjectToTaskRepository());
        context.addRepository(new TaskToProjectRepository());
        context.addRepository(new PrimitiveAttributeRepository());
        context.addRepository(new RelationIdTestRepository());
        context.addRepository(new RenamedIdRepository());
        context.addRepository(new ScheduleStatusRepositoryImpl());
        context.addRepository(new ReadOnlyTaskRepository());
        context.addRepository(new HistoricTaskRepository());
        context.addRepository(new InMemoryResourceRepository<>(NoAccessTask.class));
        context.addRepository(new InMemoryResourceRepository<>(RelocatedTask.class));
        if (extended) {
            context.addRepository(new InMemoryResourceRepository<>(ResourceIdentifierBasedRelationshipResource.class));
            context.addRepository(bulkTasks);

            context.addRepository(new InMemoryResourceRepository<>(VersionedTask.class));
        }

        context.addRepository(manyNestedRepository);
        context.addRepository(oneNestedRepository);
        context.addRepository(relatedRepository);
        context.addRepository(parentRepository);
        context.addRepository(nestedManyRelationshipRepository);
        context.addRepository(nestedOneRelationshipRepository);

        context.addNamingStrategy(new TestNamingStrategy());
        context.addExceptionMapper(new TestExceptionMapper());
    }

    /**
     * if true will expose the full set of repositories. Some omitted to not over complicate e.g. schema generation
     */
    public TestModule setExtended(boolean extended) {
        this.extended = extended;
        return this;
    }

    public ProjectRepository getProjects() {
        return projects;
    }

    public TaskRepository getTasks() {
        return tasks;
    }

    public BulkInMemoryRepository<BulkTask, Object> getBulkTasks() {
        return bulkTasks;
    }

    public static void clear() {
        TaskRepository.clear();
        ProjectRepository.clear();
        TaskToProjectRepository.clear();
        ProjectToTaskRepository.clear();
        ScheduleRepositoryImpl.clear();
        RelationIdTestRepository.clear();

        manyNestedRepository.clear();
        relatedRepository.clear();
        parentRepository.clear();
    }
}
