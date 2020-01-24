package io.crnk.core.module;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;

class TestRepository implements ResourceRepository<TestResource, Integer> {

	@Override
	public <S extends TestResource> S save(S entity) {
		return null;
	}

	@Override
	public void delete(Integer id) {
	}

	@Override
	public Class<TestResource> getResourceClass() {
		return TestResource.class;
	}

	@Override
	public TestResource findOne(Integer id, QuerySpec querySpec) {
		return null;
	}

	@Override
	public ResourceList<TestResource> findAll(QuerySpec querySpec) {
		return null;
	}

	@Override
	public ResourceList<TestResource> findAll(Collection<Integer> ids, QuerySpec querySpec) {
		return null;
	}

	@Override
	public <S extends TestResource> S create(S entity) {
		return null;
	}
}
