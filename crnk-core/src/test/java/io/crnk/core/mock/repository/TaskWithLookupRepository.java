package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.TaskWithLookup;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;

public class TaskWithLookupRepository implements ResourceRepository<TaskWithLookup, String> {

	@Override
	public Class<TaskWithLookup> getResourceClass() {
		return TaskWithLookup.class;
	}

	@Override
	public TaskWithLookup findOne(String id, QuerySpec querySpec) {
		return new TaskWithLookup()
				.setId(id)
				.setProject(new Project().setId(42L))
				.setProjectOverridden(new Project().setId(42L));
	}

	@Override
	public ResourceList<TaskWithLookup> findAll(QuerySpec querySpec) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceList<TaskWithLookup> findAll(Iterable<String> ids, QuerySpec querySpec) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends TaskWithLookup> S save(S resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends TaskWithLookup> S create(S resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String id) {
		throw new UnsupportedOperationException();
	}
}
