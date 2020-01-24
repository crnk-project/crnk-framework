package io.crnk.core.repository;

import org.junit.Test;

import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

public class ReadOnlyResourceRepositoryBaseTest {

	private ReadOnlyResourceRepositoryBase repo = new ReadOnlyResourceRepositoryBase(null) {
		@Override
		public ResourceList findAll(QuerySpec querySpec) {
			return null;
		}
	};

	@Test(expected = MethodNotAllowedException.class)
	public void save() {
		repo.save(null);
	}

	@Test(expected = MethodNotAllowedException.class)
	public void create() {
		repo.create(null);
	}

	@Test(expected = MethodNotAllowedException.class)
	public void delete() {
		repo.delete(null);
	}
}
