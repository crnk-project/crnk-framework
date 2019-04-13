package io.crnk.data.jpa.integration;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.data.jpa.AbstractJpaJerseyTest;
import io.crnk.data.jpa.JpaModule;
import io.crnk.data.jpa.model.VersionedEntity;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.OptimisticLockException;
import java.io.Serializable;

public class OptimisticLockingIntTest extends AbstractJpaJerseyTest {


	@Override
	protected void setupModule(JpaModule module, boolean server) {
		if (server) {
			module.setQueryFactory(JpaCriteriaQueryFactory.newInstance());
		}
	}

	@Test
	public void testOptimisticLocking() {
		ResourceRepository<VersionedEntity, Serializable> repo = client
				.getRepositoryForType(VersionedEntity.class);
		VersionedEntity entity = new VersionedEntity();
		entity.setId(1L);
		entity.setLongValue(13L);
		VersionedEntity saved = repo.create(entity);
		Assert.assertEquals(0, saved.getVersion());

		saved.setLongValue(14L);
		saved = repo.save(saved);
		Assert.assertEquals(1, saved.getVersion());

		saved.setLongValue(15L);
		saved = repo.save(saved);
		Assert.assertEquals(2, saved.getVersion());

		saved.setLongValue(16L);
		saved.setVersion(saved.getVersion() - 1);
		try {
			saved = repo.save(saved);
			Assert.fail();
		} catch (OptimisticLockException e) {
			// ok
		}

		VersionedEntity persisted = repo.findOne(1L, new QuerySpec(VersionedEntity.class));
		Assert.assertEquals(2, persisted.getVersion());
		Assert.assertEquals(15L, persisted.getLongValue());
	}
}
