package io.crnk.security;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.security.SecurityProvider;
import io.crnk.core.engine.security.SecurityProviderContext;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.BulkResourceRepository;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.security.SecurityConfig.Builder;
import io.crnk.security.internal.DataRoomMatcher;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.BulkTask;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.TaskStatus;
import io.crnk.test.mock.repository.BulkInMemoryRepository;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.TaskRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DataroomFilterTest {

    private SecurityModule securityModule;

    private TaskRepository tasksImpl;

    private ResourceRepository<Task, Long> tasks;

    private BulkResourceRepository<BulkTask, Long> bulkTasks;

    private Task taskFoo;

    private Task taskBar;

    private Project project;

    private RegistryEntry entry;

    private BulkInMemoryRepository<BulkTask, Object> bulkTasksImpl;

    @After
    public void tearDown() {
        TestModule.clear();
    }

    @Before
    public void setup() {
        TestModule.clear();

        // TODO simplify ones simple module is fixed
        SimpleModule appModule = new SimpleModule("app") {

            @Override
            public void setupModule(ModuleContext context) {
                super.setupModule(context);

                context.addSecurityProvider(new SecurityProvider() {
                    @Override
                    public boolean isUserInRole(String role, SecurityProviderContext context) {
                        return true;
                    }

                    @Override
                    public boolean isAuthenticated(SecurityProviderContext context) {
                        return true;
                    }
                });
            }
        };

        // tag::docs[]
        Builder builder = SecurityConfig.builder();
        builder.permitAll(ResourcePermission.ALL);
        builder.setDataRoomFilter((querySpec, method, securityProvider) -> {
            if (querySpec.getResourceClass() == Task.class || querySpec.getResourceClass() == BulkTask.class) {
                QuerySpec clone = querySpec.clone();
                clone.addFilter(PathSpec.of("name").filter(FilterOperator.EQ, "foo"));
                return clone;
            }
            return querySpec;
        });
        SecurityConfig config = builder.build();
        securityModule = SecurityModule.newServerModule(config);
        // end::docs[]
        Assert.assertSame(config, securityModule.getConfig());

        TestModule testModule = new TestModule();
        tasksImpl = testModule.getTasks();
        bulkTasksImpl = testModule.getBulkTasks();

        project = new Project();
        project.setName("someProject");
        ProjectRepository projects = testModule.getProjects();
        projects.save(project);

        CrnkBoot boot = new CrnkBoot();
        boot.addModule(securityModule);
        boot.addModule(testModule);
        boot.addModule(appModule);
        boot.boot();

        taskFoo = addTask("foo", project);
        taskBar = addTask("bar", project);

        entry = boot.getResourceRegistry().getEntry(Task.class);

        tasks = (ResourceRepository<Task, Long>) entry.getResourceRepository().getImplementation();
        bulkTasks = (BulkResourceRepository<BulkTask, Long>) boot.getResourceRegistry().getEntry(BulkTask.class).getResourceRepository().getImplementation();
    }

    private Task addTask(String name, Project project) {
        Task task = new Task();
        task.setName(name);
        task.setProject(project);
        tasksImpl.create(task);

        BulkTask bulkTask = new BulkTask();
        bulkTask.setId(task.getId());
        bulkTask.setName(name);
        bulkTasksImpl.create(bulkTask);

        return task;
    }

    @Test
    public void checkInterceptorsInPlace() {
        Assert.assertTrue(securityModule.getConfig().getPerformDataRoomChecks());
    }

    @Test
    public void manualMatching() {
        // tag::match[]
        DataRoomMatcher matcher = securityModule.getDataRoomMatcher();
        Task task = new Task();
        task.setName("foo");
        boolean match = matcher.checkMatch(task, HttpMethod.GET, securityModule.getCallerSecurityProvider());
        Assert.assertTrue(match);
        // end::match[]
    }


    @Test
    public void manualNoMatching() {
        // tag::match[]
        DataRoomMatcher matcher = securityModule.getDataRoomMatcher();
        Task task = new Task();
        task.setName("base");
        boolean match = matcher.checkMatch(task, HttpMethod.GET, securityModule.getCallerSecurityProvider());
        Assert.assertFalse(match);
        // end::match[]
    }

    @Test
    public void checkFindAll() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        ResourceList<Task> list = tasks.findAll(querySpec);
        Assert.assertEquals(1, list.size());
        Task task = list.get(0);
        Assert.assertEquals("foo", task.getName());
    }


    @Test
    public void checkFindOneAllowed() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        Task task = tasks.findOne(taskFoo.getId(), querySpec);
        Assert.assertEquals("foo", task.getName());
    }

    @Test(expected = ForbiddenException.class)
    public void checkFindOneNotAllowed() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        Task task = tasks.findOne(taskBar.getId(), querySpec);
        Assert.assertEquals("foo", task.getName());
    }

    @Test(expected = ForbiddenException.class)
    public void checkSaveNotAllowedToChangeToNonMatched() {
        Task task = new Task();
        task.setId(taskFoo.getId());
        task.setName("notFoo"); // => would make it get filtered
        tasks.save(task);
    }

    @Test(expected = ForbiddenException.class)
    public void checkSaveNotAllowedToChangeToMatched() {
        // => should not have access to bar in the first place
        // => not allowed to make it visible
        Task task = new Task();
        task.setId(taskBar.getId());
        task.setName("foo");
        tasks.save(task);
    }

    @Test
    public void checkSaveAllowed() {
        Task task = new Task();
        task.setId(taskFoo.getId());
        task.setName("foo");
        task.setStatus(TaskStatus.CLOSED);
        tasks.save(task);
    }


    @Test(expected = ForbiddenException.class)
    public void checkBulkSaveNotAllowedToChangeToNonMatched() {
        BulkTask task = new BulkTask();
        task.setId(taskFoo.getId());
        task.setName("notFoo"); // => would make it get filtered
        bulkTasks.save(Arrays.asList(task));
    }

    @Test(expected = ForbiddenException.class)
    public void checkBulkSaveNotAllowedToChangeToMatched() {
        // => should not have access to bar in the first place
        // => not allowed to make it visible
        BulkTask task = new BulkTask();
        task.setId(taskBar.getId());
        task.setName("foo");
        bulkTasks.save(Arrays.asList(task));
    }

    @Test
    public void checkBulkSaveAllowed() {
        BulkTask task = new BulkTask();
        task.setId(taskFoo.getId());
        task.setName("foo");
        bulkTasks.save(Arrays.asList(task));
    }

    @Test
    public void checkCreateAllowed() {
        Task task = new Task();
        task.setName("foo");
        task.setStatus(TaskStatus.CLOSED);
        tasks.create(task);
    }

    @Test(expected = ForbiddenException.class)
    public void checkCreateNotAllowed() {
        Task task = new Task();
        task.setName("notFoo");
        task.setStatus(TaskStatus.CLOSED);
        tasks.create(task);
    }


    @Test
    public void checkBulkCreateAllowed() {
        BulkTask task = new BulkTask();
        task.setId(123L);
        task.setName("foo");
        bulkTasks.create(Arrays.asList(task));
    }

    @Test(expected = ForbiddenException.class)
    public void checkBulkCreateNotAllowed() {
        BulkTask task = new BulkTask();
        task.setName("notFoo");
        bulkTasks.create(Arrays.asList(task));
    }


    @Test
    public void checkDeleteAllowed() {
        tasks.delete(taskFoo.getId());
    }

    @Test(expected = ForbiddenException.class)
    public void checkDeleteNotAllowed() {
        tasks.delete(taskBar.getId());
    }

    @Test
    public void checkBulkDeleteAllowed() {
        bulkTasks.delete(Arrays.asList(taskFoo.getId()));
    }

    @Test(expected = ForbiddenException.class)
    public void checkBulkDeleteNotAllowed() {
        bulkTasks.delete(Arrays.asList(taskBar.getId()));
    }

    @Test
    public void checkFindRelationshipAuthorized() {
        OneRelationshipRepository taskToProject = (OneRelationshipRepository) entry.getRelationshipRepository("project").getImplementation();
        taskToProject.setRelation(taskFoo, project.getId(), "project");

        List<Long> ids = Arrays.asList(taskFoo.getId());
        QuerySpec querySpec = new QuerySpec(Project.class);
        Map map = taskToProject.findOneRelations(ids, "project", querySpec);
        Object project = map.get(taskFoo.getId());
        Assert.assertNotNull(project);
    }

    @Test
    public void checkSetRelationshipAuthorized() {
        OneRelationshipRepository taskToProject = (OneRelationshipRepository) entry.getRelationshipRepository("project").getImplementation();
        taskToProject.setRelation(taskFoo, project.getId(), "project");
    }

    @Test(expected = ForbiddenException.class)
    public void checkSetRelationshipNotAuthorized() {
        OneRelationshipRepository taskToProject = (OneRelationshipRepository) entry.getRelationshipRepository("project").getImplementation();
        taskToProject.setRelation(taskBar, project.getId(), "project");
    }

    @Test
    public void checkFindRelationshipsAuthorized() {
        ManyRelationshipRepository taskToProject = (ManyRelationshipRepository) entry.getRelationshipRepository("project").getImplementation();

        List<Long> ids = Arrays.asList(taskFoo.getId());
        QuerySpec querySpec = new QuerySpec(Project.class);
        taskToProject.findManyRelations(ids, "includedProjects", querySpec);
    }

    @Test
    public void checkSetRelationshipsAuthorized() {
        ManyRelationshipRepository taskToProject = (ManyRelationshipRepository) entry.getRelationshipRepository("project").getImplementation();
        taskToProject.setRelations(taskFoo, Arrays.asList(project.getId()), "includedProjects");
        taskToProject.removeRelations(taskFoo, Arrays.asList(project.getId()), "includedProjects");
        taskToProject.addRelations(taskFoo, Arrays.asList(project.getId()), "includedProjects");

    }

    @Test(expected = ForbiddenException.class)
    @Ignore // assumed that opposite side is properly filtered
    public void checkFindRelationshipsNotAuthorized() {
        ManyRelationshipRepository taskToProject = (ManyRelationshipRepository) entry.getRelationshipRepository("project").getImplementation();
        List<Long> ids = Arrays.asList(taskBar.getId());
        QuerySpec querySpec = new QuerySpec(Project.class);
        taskToProject.findManyRelations(ids, "includedProjects", querySpec);
    }

    @Test(expected = ForbiddenException.class)
    @Ignore // assumed that opposite side is properly filtered
    public void checkSetRelationshipsNotAuthorized() {
        ManyRelationshipRepository taskToProject = (ManyRelationshipRepository) entry.getRelationshipRepository("project").getImplementation();
        taskToProject.setRelations(taskBar, Arrays.asList(project.getId()), "includedProjects");
    }
}
