package io.crnk.client.inheritance.repositories.related;

import io.crnk.client.inheritance.repositories.RepositoryData;
import io.crnk.client.inheritance.resources.related.RelatedResourceB;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

/*
 * @author syri.
 */
public class RelatedResourceBRepository extends ResourceRepositoryBase<RelatedResourceB, Long> {

	public RelatedResourceBRepository() {
		super(RelatedResourceB.class);
	}

	@Override
	public ResourceList<RelatedResourceB> findAll(QuerySpec querySpec) {
		return querySpec.apply(RepositoryData.RESOURCE_B_LIST);
	}
}
