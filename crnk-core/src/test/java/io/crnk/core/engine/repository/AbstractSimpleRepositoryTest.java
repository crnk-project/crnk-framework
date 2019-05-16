package io.crnk.core.engine.repository;

import io.crnk.core.mock.models.Task;
import io.crnk.legacy.repository.AbstractSimpleLegacyRepository;
import org.junit.Test;

public class AbstractSimpleRepositoryTest {

	private AbstractSimpleLegacyRepository<Task, Long> repo = new AbstractSimpleLegacyRepository<Task, Long>() {
	};

	@Test(expected = UnsupportedOperationException.class)
	public void findOne() {
		repo.findOne(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void findOneWithParams() {
		repo.findOne(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void findAll() {
		repo.findAll(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void findAllWithIds() {
		repo.findAll(null, null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void save() {
		repo.save(null);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void delete() {
		repo.delete(null);
	}

}
