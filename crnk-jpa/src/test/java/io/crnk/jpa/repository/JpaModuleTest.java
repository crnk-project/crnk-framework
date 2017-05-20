package io.crnk.jpa.repository;

import io.crnk.jpa.JpaModule;
import io.crnk.jpa.model.TestEntity;
import io.crnk.jpa.query.AbstractJpaTest;
import io.crnk.jpa.query.JpaQueryFactory;
import io.crnk.jpa.query.querydsl.QuerydslQueryFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Set;

@Transactional
public class JpaModuleTest extends AbstractJpaTest {

	@Override
	protected void setupModule(JpaModule module) {
		Set<Class<?>> resourceClasses = module.getResourceClasses();
		int n = resourceClasses.size();
		Assert.assertNotEquals(0, n);
		module.removeRepository(TestEntity.class);
		Assert.assertEquals(n - 1, module.getResourceClasses().size());
		module.removeRepositories();
	}

	@Test
	public void test() throws InstantiationException, IllegalAccessException {
		Assert.assertEquals(0, module.getResourceClasses().size());

		Assert.assertEquals("jpa", module.getModuleName());

		Assert.assertNotNull(module.getEntityManagerFactory());
	}

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}

}
