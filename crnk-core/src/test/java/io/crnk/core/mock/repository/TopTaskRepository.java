package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.TopTask;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

public class TopTaskRepository extends ReadOnlyResourceRepositoryBase<TopTask, Long> {

	public TopTaskRepository() {
		super(TopTask.class);
	}

	@Override
	public ResourceList<TopTask> findAll(final QuerySpec querySpec) {
		return null;
	}
}