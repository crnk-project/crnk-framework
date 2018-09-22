package io.crnk.core.mock.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.MetaRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.legacy.queryParams.QueryParams;

public class TaskRepository implements ResourceRepositoryV2<Task, Long>, MetaRepositoryV2<Task> {

	private static final ConcurrentHashMap<Long, Task> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();
	}

	@Override
	public <S extends Task> S save(S entity) {
		if ("badName".equals(entity.getName())) {
			throw new BadRequestException("badName not a valid name");
		}
		if (entity.getId() == null) {
			entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
		}
		THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

		return entity;
	}

	@Override
	public <S extends Task> S create(S resource) {
		return save(resource);
	}

	@Override
	public Class<Task> getResourceClass() {
		return Task.class;
	}

	@Override
	public Task findOne(Long aLong, QuerySpec querySpec) {
		Task task = THREAD_LOCAL_REPOSITORY.get(aLong);
		if (task == null) {
			throw new ResourceNotFoundException("");
		}
		return task;
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec querySpec) {
		DefaultResourceList<Task> list = new DefaultResourceList<>();
		list.addAll(THREAD_LOCAL_REPOSITORY.values());
		return list;
	}

	@Override
	public ResourceList<Task> findAll(Iterable<Long> ids, QuerySpec querySpec) {
		List<Task> values = new LinkedList<>();
		for (Task value : THREAD_LOCAL_REPOSITORY.values()) {
			if (contains(value, ids)) {
				values.add(value);
			}
		}

		DefaultResourceList<Task> list = new DefaultResourceList<>();
		list.addAll(values);
		return list;
	}

	private boolean contains(Task value, Iterable<Long> ids) {
		for (Long id : ids) {
			if (value.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void delete(Long aLong) {
		THREAD_LOCAL_REPOSITORY.remove(aLong);
	}

	@Override
	public MetaInformation getMetaInformation(Iterable<Task> resources, QuerySpec querySpec) {
		return new MetaData();
	}

	public static class MetaData implements MetaInformation {

		public String someValue;
	}
}
