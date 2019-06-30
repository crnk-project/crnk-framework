package io.crnk.core.repository.forward;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.RelationIdTestResource;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.RelationIdTestRepository;
import io.crnk.core.mock.repository.TaskList;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.foward.ForwardingDirection;
import io.crnk.core.repository.foward.ForwardingRelationshipRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OppositeFowardingRelationshipRepositoryTest {


    private ForwardingRelationshipRepository relRepository;

    private RelationIdTestRepository testRepository;

    private ResourceRegistry resourceRegistry;

    private HttpRequestContextProvider requestContextProvider;
    private CoreTestContainer container;

    @Before
    public void setup() {
        container = new CoreTestContainer();
        container.addModule(new CoreTestModule());
        container.boot();
        resourceRegistry = container.getResourceRegistry();


        testRepository = (RelationIdTestRepository) container.getEntry(RelationIdTestResource.class)
                .getResourceRepository().getResourceRepository();
        requestContextProvider = container.getModuleRegistry().getHttpRequestContextProvider();

        RelationshipMatcher relMatcher =
                new RelationshipMatcher().rule().source(RelationIdTestResource.class).target(RelationIdTestResource.class).add();
        relRepository = new ForwardingRelationshipRepository(RelationIdTestResource.class, relMatcher,
                ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
        relRepository.setResourceRegistry(container.getResourceRegistry());
        relRepository.setHttpRequestContextProvider(requestContextProvider);
    }

    @Test
    public void checkFindOneTarget() {
        RelationIdTestResource parent = new RelationIdTestResource();
        parent.setId(2L);
        parent.setName("parent");

        RelationIdTestResource child = new RelationIdTestResource();
        child.setId(3L);
        child.setName("child");

        parent.setTestNested(child);
        testRepository.create(parent);
        testRepository.create(child);

        QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
        Object target = relRepository.findOneTarget(3L, "testNestedOpposite", querySpec);

        Assert.assertNotNull(target);
    }


    @Test
    public void checkFindOneTargetWithRelationId() {
        RelationIdTestResource parent = new RelationIdTestResource();
        parent.setId(2L);
        parent.setName("parent");

        RelationIdTestResource child = new RelationIdTestResource();
        child.setId(3L);
        child.setName("child");

        parent.setTestNestedId(child.getId());
        testRepository.create(parent);
        testRepository.create(child);

        QuerySpec querySpec = new QuerySpec(RelationIdTestResource.class);
        Object target = relRepository.findOneTarget(3L, "testNestedOpposite", querySpec);

        Assert.assertNotNull(target);
    }

    @Test
    public void checkFindManyTargets() {
        ResourceRepository<Project, Object> projectRepository = container.getRepository(Project.class);
        Project project = new Project();
        project.setId(42L);
        project.setName("project");
        projectRepository.save(project);

        ResourceRepository<Task, Object> taskRepository = container.getRepository(Task.class);
        Task task = new Task();
        task.setId(13L);
        task.setName("task");
        task.setProject(project);
        taskRepository.save(task);

        relRepository = new ForwardingRelationshipRepository(Project.class, null,
                ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
        relRepository.setResourceRegistry(resourceRegistry);
        relRepository.setHttpRequestContextProvider(requestContextProvider);

        QuerySpec querySpec = new QuerySpec(Task.class);
        List<Task> tasks = relRepository.findManyTargets(42L, "tasks", querySpec);
        Assert.assertEquals(1, tasks.size());
        Assert.assertEquals(13L, tasks.get(0).getId().longValue());

        // must maintain meta/links information
        Assert.assertTrue(tasks instanceof TaskList);
    }

    @Test
    public void checkFindManyTargetsWithRelationId() {
        ResourceRepository<Project, Object> projectRepository = container.getRepository(Project.class);
        Project project = new Project();
        project.setId(42L);
        project.setName("project");
        projectRepository.save(project);

        ResourceRepository<Schedule, Object> scheduleRepository = container.getRepository(Schedule.class);
        Schedule schedule = new Schedule();
        schedule.setId(13L);
        schedule.setName("schedule");
        schedule.setProjectId(project.getId());
        scheduleRepository.save(schedule);

        relRepository = new ForwardingRelationshipRepository(Project.class, null,
                ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
        relRepository.setResourceRegistry(resourceRegistry);
        relRepository.setHttpRequestContextProvider(requestContextProvider);

        QuerySpec querySpec = new QuerySpec(Schedule.class);
        List<Schedule> schedules = relRepository.findManyTargets(42L, "schedules", querySpec);
        Assert.assertEquals(1, schedules.size());
        Assert.assertEquals(13L, schedules.get(0).getId().longValue());
    }


    @Test
    public void checkFindOneTargetFromCollection() {
        ResourceRepository<Task, Object> taskRepository = container.getRepository(Task.class);
        Task task = new Task();
        task.setId(13L);
        task.setName("task");
        taskRepository.save(task);

        ResourceRepository<Project, Object> projectRepository = container.getRepository(Project.class);
        Project project = new Project();
        project.setId(42L);
        project.setName("project");
        project.setTasks(Arrays.asList(task));
        projectRepository.save(project);

        relRepository = new ForwardingRelationshipRepository(Task.class, null,
                ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
        relRepository.setResourceRegistry(resourceRegistry);
        relRepository.setHttpRequestContextProvider(requestContextProvider);

        QuerySpec querySpec = new QuerySpec(Task.class);
        Project foundProject = (Project) relRepository.findOneTarget(13L, "project", querySpec);
        Assert.assertEquals(42L, foundProject.getId().longValue());
    }

    @Test
    @Ignore
    public void checkFindTargetWithInvalidNullReturnId() {
        ResourceRepository<Task, Object> taskRepository = container.getRepository(Task.class);
        Task task = new Task();
        task.setId(13L);
        task.setName("task");
        taskRepository.save(task);

        ResourceRepository<Project, Object> projectRepository = container.getRepository(Project.class);
        Project project = new Project();
        project.setId(42L);
        project.setName("project");
        project.setTasks(Arrays.asList(task));
        projectRepository.save(project);

        // manipulate
        task.setId(null);

        relRepository = new ForwardingRelationshipRepository(Task.class, null,
                ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
        relRepository.setResourceRegistry(resourceRegistry);
        relRepository.setHttpRequestContextProvider(requestContextProvider);

        QuerySpec querySpec = new QuerySpec(Task.class);
        try {
            relRepository.findOneTarget(13L, "project", querySpec);
            Assert.fail();
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("id is null"));
        }
    }

    @Test
    @Ignore
    public void checkFindTargetWithNotLoadedRelationship() {
        ResourceRepository<Task, Object> taskRepository = container.getRepository(Task.class);
        Task task = new Task();
        task.setId(13L);
        task.setName("task");
        taskRepository.save(task);

        ResourceRepository<Project, Object> projectRepository = container.getRepository(Project.class);
        Project project = new Project();
        project.setId(42L);
        project.setName("project");
        project.setTasks(null);
        projectRepository.save(project);

        relRepository = new ForwardingRelationshipRepository(Task.class, null,
                ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
        relRepository.setResourceRegistry(resourceRegistry);
        relRepository.setHttpRequestContextProvider(requestContextProvider);

        QuerySpec querySpec = new QuerySpec(Task.class);
        try {
            relRepository.findOneTarget(13L, "project", querySpec);
            Assert.fail();
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().contains("To make use of opposite forwarding behavior for resource lookup"));
        }
    }

    @Test
    @Ignore
    public void checkFindTargetWithNullRelationshipValue() {
        ResourceRepository<Task, Object> taskRepository = container.getRepository(Task.class);
        Task task = new Task();
        task.setId(13L);
        task.setName("task");
        taskRepository.save(task);

        Task nullIdTask = new Task();

        ResourceRepository<Project, Object> projectRepository = container.getRepository(Project.class);
        Project project = new Project();
        project.setId(42L);
        project.setName("project");
        project.setTasks(Arrays.asList(nullIdTask));
        projectRepository.save(project);

        relRepository = new ForwardingRelationshipRepository(Task.class, null,
                ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
        relRepository.setResourceRegistry(resourceRegistry);
        relRepository.setHttpRequestContextProvider(requestContextProvider);

        QuerySpec querySpec = new QuerySpec(Task.class);
        try {
            relRepository.findOneTarget(13L, "project", querySpec);
            Assert.fail();
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("id is null for"));
        }
    }

    @Test
    public void checkAddRemoveRelations() {
        ResourceRepository<Task, Object> taskRepository = container.getRepository(Task.class);
        List<Task> tasks = new ArrayList<>();
        for (long i = 0; i < 10; i++) {
            Task task = new Task();
            task.setId(i);
            task.setName("task");
            taskRepository.save(task);
            tasks.add(task);
        }

        ResourceRepository<Project, Object> projectRepository = container.getRepository(Project.class);
        Project project = new Project();
        project.setId(42L);
        project.setName("project");
        projectRepository.save(project);

        relRepository = new ForwardingRelationshipRepository(Project.class, null,
                ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
        relRepository.setResourceRegistry(resourceRegistry);
        relRepository.setHttpRequestContextProvider(requestContextProvider);

        Assert.assertNull(tasks.get(3).getProject());
        Assert.assertNull(tasks.get(4).getProject());
        relRepository.addRelations(project, Arrays.asList(3L, 4L), "tasks");
        Assert.assertSame(project, tasks.get(3).getProject());
        Assert.assertSame(project, tasks.get(4).getProject());

        relRepository.addRelations(project, Arrays.asList(5L), "tasks");
        Assert.assertSame(project, tasks.get(3).getProject());
        Assert.assertSame(project, tasks.get(4).getProject());
        Assert.assertSame(project, tasks.get(5).getProject());

        relRepository.removeRelations(project, Arrays.asList(3L), "tasks");
        Assert.assertNull(tasks.get(3).getProject());
        Assert.assertSame(project, tasks.get(4).getProject());
        Assert.assertSame(project, tasks.get(5).getProject());
    }

    @Test
    public void checkSetRelations() {
        ResourceRepository<Task, Object> taskRepository = container.getRepository(Task.class);
        Task task = new Task();
        task.setId(13L);
        task.setName("task");
        taskRepository.save(task);

        ResourceRepository<Project, Object> projectRepository = container.getRepository(Project.class);
        Project project = new Project();
        project.setId(42L);
        project.setName("project");
        projectRepository.save(project);

        relRepository = new ForwardingRelationshipRepository(Task.class, null,
                ForwardingDirection.OPPOSITE, ForwardingDirection.OPPOSITE);
        relRepository.setResourceRegistry(resourceRegistry);
        relRepository.setHttpRequestContextProvider(requestContextProvider);

        relRepository.setRelation(task, project.getId(), "project");
        Assert.assertTrue(project.getTasks().contains(task));

        // setup bi-directionality to allow removal
        task.setProject(project);
        relRepository.setRelation(task, null, "project");
        Assert.assertFalse(project.getTasks().contains(task));

        // verify repeated removal has no impact
        task.setProject(null);
        relRepository.setRelation(task, null, "project");
        Assert.assertFalse(project.getTasks().contains(task));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void checkSetRelationsNotYetImplemented() {
        relRepository.setRelations(null, null, null);
    }
}
