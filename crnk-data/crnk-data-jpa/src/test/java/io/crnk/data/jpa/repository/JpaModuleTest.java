package io.crnk.data.jpa.repository;

import io.crnk.data.jpa.JpaModule;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.data.jpa.model.TestEntity;
import io.crnk.data.jpa.query.AbstractJpaTest;
import io.crnk.data.jpa.query.JpaQueryFactory;
import io.crnk.data.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Set;

@Transactional
public class JpaModuleTest extends AbstractJpaTest {

    @Test
    public void hasProtectedConstructor() {
        ClassTestUtils.assertProtectedConstructor(JpaModule.class);
    }


    @Override
    protected void setupModule(JpaModuleConfig module) {
        Set<Class<?>> resourceClasses = module.getResourceClasses();
        int n = resourceClasses.size();
        Assert.assertNotEquals(0, n);
        module.removeRepository(TestEntity.class);
        Assert.assertEquals(n - 1, module.getResourceClasses().size());
        module.removeRepositories();
    }

    @Test
    public void test() {
        Assert.assertEquals(0, module.getConfig().getResourceClasses().size());

        Assert.assertEquals("jpa", module.getModuleName());
    }

    @Override
    protected JpaQueryFactory createQueryFactory(EntityManager em) {
        return QuerydslQueryFactory.newInstance();
    }

}
