package io.crnk.test.mock.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;

public class ScheduleRepositoryImpl extends ResourceRepositoryBase<Schedule, Long> implements ScheduleRepository {

	private static Map<Long, Schedule> schedules = new HashMap<>();

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
		Schedule schedule = new Schedule();
		schedule.setId(1L);
		schedule.setName(msg);
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
		list.addAll(querySpec.apply(schedules.values()));
		list.setLinks(new ScheduleListLinks());
		list.setMeta(new ScheduleListMeta());
		return list;
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