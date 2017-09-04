package io.crnk.core.queryspec.repository;

import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.repository.ScheduleRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;

import java.util.HashMap;
import java.util.Map;

public class ScheduleQuerySpecRepository extends ResourceRepositoryBase<Schedule, Long> {

	private static Map<Long, Schedule> schedules = new HashMap<>();

	public ScheduleQuerySpecRepository() {
		super(Schedule.class);
	}

	public static void clear() {
		schedules.clear();
	}

	@Override
	public ScheduleRepository.ScheduleList findAll(QuerySpec querySpec) {
		ScheduleRepository.ScheduleList list = new ScheduleRepository.ScheduleList();
		list.addAll(querySpec.apply(schedules.values()));
		list.setLinks(new ScheduleRepository.ScheduleListLinks());
		list.setMeta(new ScheduleRepository.ScheduleListMeta());
		return list;
	}

	@Override
	public <S extends Schedule> S save(S entity) {
		schedules.put(entity.getId(), entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		schedules.remove(id);
	}
}