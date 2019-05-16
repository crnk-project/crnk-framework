package io.crnk.client.inheritance.repositories.cyclic;

import java.util.Collections;

import io.crnk.client.inheritance.resources.cyclic.CyclicResourceA;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

/*
 * @author syri.
 */
public class CyclicResourceARepository extends ResourceRepositoryBase<CyclicResourceA, Long> {

	public CyclicResourceARepository() {
		super(CyclicResourceA.class);
	}

	@Override
	public ResourceList<CyclicResourceA> findAll(QuerySpec querySpec) {
		return querySpec.apply(Collections.emptyList());
	}
}
