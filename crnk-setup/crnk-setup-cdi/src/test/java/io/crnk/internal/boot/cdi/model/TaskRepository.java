package io.crnk.internal.boot.cdi.model;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.annotations.JsonApiExposed;

import javax.enterprise.context.ApplicationScoped;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@JsonApiExposed
public class TaskRepository {

	private static final ConcurrentHashMap<Long, Task> map = new ConcurrentHashMap<>();

	public static void clear() {
		map.clear();
	}

	public <S extends Task> S save(S entity) {

		if (entity.getId() == null) {
			entity.setId((long) (map.size() + 1));
		}
		map.put(entity.getId(), entity);

		return entity;
	}

	public Task findOne(Long aLong, QuerySpec querySpec) {
		Task task = map.get(aLong);
		if (task == null) {
			throw new ResourceNotFoundException("");
		}
		return task;
	}

	public Iterable<Task> findAll(QuerySpec queryParams) {
		return queryParams.apply(map.values());
	}

	public Iterable<Task> findAll(Iterable<Long> ids, QuerySpec queryParams) {
		List<Task> values = new LinkedList<>();
		for (Task value : map.values()) {
			if (contains(value, ids)) {
				values.add(value);
			}
		}
		return values;
	}

	private boolean contains(Task value, Iterable<Long> ids) {
		for (Long id : ids) {
			if (value.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	public void delete(Long aLong) {
		map.remove(aLong);
	}
}
