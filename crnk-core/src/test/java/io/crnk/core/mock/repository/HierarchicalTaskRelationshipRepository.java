package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.HierarchicalTask;
import io.crnk.core.repository.RelationshipRepositoryBase;

public class HierarchicalTaskRelationshipRepository
		extends RelationshipRepositoryBase<HierarchicalTask, Long, HierarchicalTask, Long> {


	public HierarchicalTaskRelationshipRepository() {
		super(HierarchicalTask.class, HierarchicalTask.class);
	}
}