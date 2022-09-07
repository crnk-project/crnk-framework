package io.crnk.core.mock.repository;

import java.util.HashSet;
import java.util.Set;

import io.crnk.core.mock.models.SpecialTask;
import io.crnk.core.mock.models.SuperTask;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

public class SuperTaskRepository extends ReadOnlyResourceRepositoryBase<SuperTask, Long> {

	private Set<SpecialTask> tasks = new HashSet<>();

	private long nextId = 0;

	public SuperTaskRepository() {
		super(SuperTask.class);
	}

	@Override
	public ResourceList<SuperTask> findAll(final QuerySpec querySpec) {
		return null;
	}
}