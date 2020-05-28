package io.crnk.client;

import io.crnk.client.internal.proxy.ObjectProxy;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.ScheduleStatus;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Collection;

public abstract class AbstractProxiedObjectsClientTest extends AbstractClientTest {

    protected ResourceRepository<Task, Long> taskRepo;

    protected ResourceRepository<Project, Long> projectRepo;

    protected RelationshipRepository<Task, Long, Project, Long> relRepo;

    private ResourceRepository<Schedule, Long> scheduleRepo;

    private RelationshipRepository<Schedule, Serializable, Task, Serializable> scheduleTaskRepo;

    private RelationshipRepository<Task, Serializable, Schedule, Serializable> taskScheduleRepo;

    private ResourceRepository<ScheduleStatus, Object> statusRepo;

    private RelationshipRepository<Schedule, Object, Project, Object> scheduleProjectRepo;

    private RelationshipRepository<Task, Object, Project, Object> taskProjectRepo;

    @Before
    public void setup() {
        super.setup();

        scheduleRepo = client.getRepositoryForType(Schedule.class);
        taskRepo = client.getRepositoryForType(Task.class);
        projectRepo = client.getRepositoryForType(Project.class);
        statusRepo = client.getRepositoryForType(ScheduleStatus.class);
        relRepo = client.getRepositoryForType(Task.class, Project.class);
        scheduleTaskRepo = client.getRepositoryForType(Schedule.class, Task.class);
        scheduleProjectRepo = client.getRepositoryForType(Schedule.class, Project.class);
        taskScheduleRepo = client.getRepositoryForType(Task.class, Schedule.class);
        taskProjectRepo = client.getRepositoryForType(Task.class, Project.class);
    }

    @Override
    protected TestApplication configure() {
        return new TestApplication();
    }

    @Test
    public void noProxyForLazy() {
        Task task = new Task();
        task.setId(1L);
        task.setName("project");
        taskRepo.create(task);

        Project project = new Project();
        project.setId(2L);
        project.setName("test");
        projectRepo.create(project);

        taskProjectRepo.setRelation(task, project.getId(), "project");

        QuerySpec querySpec = new QuerySpec(Task.class);
        task = taskRepo.findOne(1L, querySpec);
        Project proxiedObject = task.getProject();
        Assert.assertNull(proxiedObject);

    }

    @Test
    public void noproxyForIdFieldAndSerializedId() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setName("project");
        scheduleRepo.create(schedule);

        Project project = new Project();
        project.setId(2L);
        project.setName("test");
        projectRepo.create(project);

        scheduleProjectRepo.setRelation(schedule, project.getId(), "project");

        QuerySpec querySpec = new QuerySpec(Schedule.class);
        schedule = scheduleRepo.findOne(1L, querySpec);
        Project proxiedObject = schedule.getProject();
        Assert.assertNull(proxiedObject);
        Assert.assertEquals(Long.valueOf(2L), schedule.getProjectId());
    }


    @Test
    public void proxyForSerializedIdWithoutRelationId() {
        Task task = new Task();
        task.setId(1L);
        task.setName("project");
        taskRepo.create(task);

        Schedule schedule = new Schedule();
        schedule.setId(2L);
        schedule.setName("test");
        scheduleRepo.create(schedule);

        taskScheduleRepo.setRelation(task, schedule.getId(), "schedule");

        QuerySpec querySpec = new QuerySpec(Task.class);
        task = taskRepo.findOne(1L, querySpec);
        Schedule proxiedObject = task.getSchedule();
        Assert.assertNotNull(proxiedObject);
        Assert.assertEquals(2L, proxiedObject.getId().longValue());
        Assert.assertNull(proxiedObject.getName());
    }


    @Test
    public void noProxyForEager() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setName("project");
        scheduleRepo.create(schedule);

        ScheduleStatus status = new ScheduleStatus();
        status.setId(2L);
        status.setDescription("test");
        statusRepo.create(status);

        scheduleTaskRepo.setRelation(schedule, status.getId(), "status");

        QuerySpec querySpec = new QuerySpec(Schedule.class);
        schedule = scheduleRepo.findOne(1L, querySpec);
        ScheduleStatus proxiedObject = schedule.getStatus();
        Assert.assertNotNull(proxiedObject);
        Assert.assertEquals(2L, proxiedObject.getId().longValue());
        Assert.assertNotNull(proxiedObject.getDescription());

    }


    @Test
    public void proxyForLazySet() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setName("project");
        scheduleRepo.create(schedule);

        Task task = new Task();
        task.setId(2L);
        task.setName("test");
        taskRepo.create(task);

        taskScheduleRepo.setRelation(task, schedule.getId(), "schedule");

        // collection must be available as proxy
        QuerySpec querySpec = new QuerySpec(Schedule.class);
        schedule = scheduleRepo.findOne(1L, querySpec);
        Collection<Task> proxiedTasks = schedule.getTasks();
        Assert.assertNotNull(proxiedTasks);

        // check status without loading
        ObjectProxy proxy = (ObjectProxy) proxiedTasks;
        Assert.assertFalse(proxy.isLoaded());
        Assert.assertNotNull(proxy.getUrl());
        Assert.assertFalse(proxy.isLoaded());

        // lazy load
        Assert.assertEquals(1, proxiedTasks.size());
        Assert.assertTrue(proxy.isLoaded());
        task = proxiedTasks.iterator().next();
        Assert.assertEquals(2L, task.getId().longValue());
    }

    @Test
    public void saveDoesNotTriggerLazyLoad() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setName("project");
        scheduleRepo.create(schedule);

        QuerySpec querySpec = new QuerySpec(Schedule.class);
        schedule = scheduleRepo.findOne(1L, querySpec);
        Collection<Task> proxiedTasks = schedule.getTasks();
        ObjectProxy proxy = (ObjectProxy) proxiedTasks;
        Assert.assertFalse(proxy.isLoaded());

        // update primitive field
        schedule.setName("newValue");
        scheduleRepo.save(schedule);

        // save should not trigger a load of the relation
        Assert.assertFalse(proxy.isLoaded());
        Assert.assertSame(proxy, schedule.getTasks());

        // data should be saved
        schedule = scheduleRepo.findOne(1L, querySpec);
        Assert.assertEquals("newValue", schedule.getName());
    }

    @Test
    public void saveLazyCollectionChange() {
        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setName("project");
        scheduleRepo.create(schedule);

        Task task = new Task();
        task.setId(2L);
        task.setName("test");
        taskRepo.create(task);

        QuerySpec querySpec = new QuerySpec(Schedule.class);
        schedule = scheduleRepo.findOne(1L, querySpec);
        Collection<Task> proxiedTasks = schedule.getTasks();
        ObjectProxy proxy = (ObjectProxy) proxiedTasks;
        Assert.assertFalse(proxy.isLoaded());

        // add task to collection
        proxiedTasks.add(task);
        Assert.assertTrue(proxy.isLoaded());
        Assert.assertEquals(1, proxiedTasks.size());
        scheduleRepo.save(schedule);

        schedule = scheduleRepo.findOne(1L, querySpec);
        proxiedTasks = schedule.getTasks();
        Assert.assertEquals(1, proxiedTasks.size());

        // remove task from collection
        proxiedTasks.remove(task);
        Assert.assertEquals(1, proxiedTasks.size());
        scheduleRepo.save(schedule);

        schedule = scheduleRepo.findOne(1L, querySpec);
        proxiedTasks = schedule.getTasks();
        Assert.assertEquals(1, proxiedTasks.size());
    }
}