package io.crnk.test.mock.repository;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ScheduleRepositoryImpl extends ResourceRepositoryBase<Schedule, Long> implements ScheduleRepository {

    public static Map<Long, Schedule> schedules = new HashMap<>();

    private AtomicLong nextId = new AtomicLong(1000);

    public ScheduleRepositoryImpl() {
        super(Schedule.class);
    }

    public static void clear() {
        schedules.clear();
    }

    @Override
    // Explicit @Produces annotation -> produces text/html
    public String repositoryAction(String msg) {
        return "repository action: " + msg;
    }

    @Override
    // Uses default @Produces annotation -> produces json-api
    public String repositoryActionJsonApi(String msg) {
        return "repository action: " + msg;
    }

    @Override
    public String repositoryActionWithJsonApiResponse(String msg) {
        return "repository action: " + msg;
    }

    @Override
    public String resourceAction(long id, String msg) {
        Schedule schedule = findOne(id, new QuerySpec(Schedule.class));
        return "resource action: " + msg + "@" + schedule.getName();
    }

    @Override
    public Schedule repositoryActionWithResourceResult(String msg) {
        ProjectRepository repository = new ProjectRepository();
        Iterable<Project> projects = repository.findAll(new QueryParams());
        Iterator<Project> iterator = projects.iterator();


        Schedule schedule = new Schedule();
        schedule.setId(1L);
        schedule.setName(msg);
        if (iterator.hasNext()) {
            Project project = iterator.next();
            schedule.setProjectId(project.getId());
        }
        return schedule;
    }

    @Override
    // Explicit @Produces annotation -> produces text/html
    public String repositoryActionWithNullResponse() {
        return null;
    }

    @Override
    public String repositoryActionWithNullResponseJsonApi() {
        return null;
    }

    @GET
    @Path("nonInterfaceMethodWithNullResponseJsonApi")
    @Produces(HttpHeaders.JSONAPI_CONTENT_TYPE)
    public String nonInterfaceMethodWithNullResponseJsonApi() {
        return null;
    }

    @Override
    public ScheduleList findAll(QuerySpec querySpec) {
        ScheduleList list = new ScheduleList();
        ScheduleListLinks linksInfo = new ScheduleListLinks();
        list.setLinks(linksInfo);
        list.setMeta(new ScheduleListMeta());
        querySpec.apply(copyResources(schedules.values()), list);
        return list;
    }

    private List<Schedule> copyResources(Collection<Schedule> values) {
        ArrayList<Schedule> copiedList = new ArrayList<>();
        for (Schedule schedule : values) {
            Schedule copy = new Schedule();
            copy.setId(schedule.getId());
            copy.setName(schedule.getName());
            copy.setDesc(schedule.getDesc());
            copy.setCustomData(schedule.getCustomData());
            copy.setTasks(schedule.getTasks());
            copy.setDelayed(schedule.isDelayed());
            copy.setStatus(schedule.getStatus());
            if (schedule.getProject() != null) {
                copy.setProject(schedule.getProject());
            } else {
                copy.setProjectId(schedule.getProjectId());
            }
            if (schedule.getProjects() != null) {
                copy.setProjects(schedule.getProjects());
            } else {
                copy.setProjectIds(schedule.getProjectIds());
            }
            copiedList.add(copy);
        }
        return copiedList;
    }

    @Override
    public <S extends Schedule> S create(S entity) {
        if (entity.getId() != null && entity.getId() == 10000) {
            throw new TestException("msg");
        }

        if (entity.getId() == null) {
            entity.setId(nextId.incrementAndGet());
        }
        return save(entity);
    }

    @Override
    public <S extends Schedule> S save(S entity) {
        schedules.put(entity.getId(), entity);

        if (entity.getTasks() != null) {
            for (Task task : entity.getTasks()) {
                task.setSchedule(entity);
            }
        }

        return entity;
    }

    @Override
    public void delete(Long id) {
        schedules.remove(id);
    }

    @Override
    public Schedule repositoryActionWithException(String msg) {
        throw new ForbiddenException(msg);
    }
}