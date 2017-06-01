package io.crnk.core.mock.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Task;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.annotations.JsonApiDelete;
import io.crnk.legacy.repository.annotations.JsonApiFindAll;
import io.crnk.legacy.repository.annotations.JsonApiFindAllWithIds;
import io.crnk.legacy.repository.annotations.JsonApiFindOne;
import io.crnk.legacy.repository.annotations.JsonApiMeta;
import io.crnk.legacy.repository.annotations.JsonApiResourceRepository;
import io.crnk.legacy.repository.annotations.JsonApiSave;

@JsonApiResourceRepository(Task.class)
public class TaskRepository {

	private static final ConcurrentHashMap<Long, Task> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();
	}

	@JsonApiSave
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

	@JsonApiFindOne
	public Task findOne(Long aLong, QueryParams queryParams) {
		Task task = THREAD_LOCAL_REPOSITORY.get(aLong);
		if (task == null) {
			throw new ResourceNotFoundException("");
		}
		return task;
	}

	@JsonApiFindAll
	public Iterable<Task> findAll(QueryParams queryParams) {
		return THREAD_LOCAL_REPOSITORY.values();
	}


	@JsonApiFindAllWithIds
	public Iterable<Task> findAll(Iterable<Long> ids, QueryParams queryParams) {
		List<Task> values = new LinkedList<>();
		for (Task value : THREAD_LOCAL_REPOSITORY.values()) {
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

	@JsonApiDelete
	public void delete(Long aLong) {
		THREAD_LOCAL_REPOSITORY.remove(aLong);
	}

	@JsonApiMeta
	public MetaInformation getMetaInformation(Iterable<Task> resources, QueryParams queryParams) {
		return new MetaData();
	}

	public static class MetaData implements MetaInformation {

		public String someValue;
	}
}
