package io.crnk.test.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.SimpleTask;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@JsonApiExposed
public class SimpleTaskRepository implements ResourceRepository<SimpleTask, Long> {

	private static final ConcurrentHashMap<Long, SimpleTask> map = new ConcurrentHashMap<>();

	public static void clear() {
		map.clear();
	}

	public <S extends SimpleTask> S save(S entity) {

		if (entity.getId() == null) {
			entity.setId((long) (map.size() + 1));
		}
		map.put(entity.getId(), entity);

		return entity;
	}

	@Override
	public <S extends SimpleTask> S create(S resource) {
		return save(resource);
	}

	@Override
	public Class<SimpleTask> getResourceClass() {
		return SimpleTask.class;
	}

	public SimpleTask findOne(Long aLong, QuerySpec querySpec) {
		SimpleTask task = map.get(aLong);
		if (task == null) {
			throw new ResourceNotFoundException("failed to find resource with id " + aLong);
		}
		return task;
	}

	@Override
	public ResourceList<SimpleTask> findAll(QuerySpec querySpec) {
		return querySpec.apply(map.values());
	}

	@Override
	public ResourceList<SimpleTask> findAll(Collection<Long> ids, QuerySpec queryParams) {
		List<SimpleTask> querySpec = new LinkedList<>();
		for (SimpleTask value : map.values()) {
			if (contains(value, ids)) {
				querySpec.add(value);
			}
		}
		return queryParams.apply(querySpec);
	}

	private boolean contains(SimpleTask value, Collection<Long> ids) {
		for (Long id : ids) {
			if (value.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void delete(Long aLong) {
		map.remove(aLong);
	}
}
