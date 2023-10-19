package io.crnk.internal.boot.cdi;

import io.crnk.cdi.internal.CdiServiceDiscovery;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.module.discovery.DefaultServiceDiscoveryFactory;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.repository.Repository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.internal.boot.cdi.model.CdiTestExceptionMapper;
import io.crnk.internal.boot.cdi.model.ProjectRepository;
import io.crnk.internal.boot.cdi.model.TaskRepository;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import java.util.List;
import java.util.Optional;

@RunWith(CdiTestRunner.class)
@ApplicationScoped
public class CdiServiceDiscoveryTest {

	@Test
	public void testSetter() {
		CdiServiceDiscovery discovery = new CdiServiceDiscovery();

		BeanManager beanManager = discovery.getBeanManager();
		Assert.assertNotNull(discovery.getBeanManager());

		BeanManager mock = Mockito.mock(BeanManager.class);
		discovery.setBeanManager(mock);
		Assert.assertSame(mock, discovery.getBeanManager());
		Assert.assertNotSame(mock, beanManager);
	}

	@Test
	public void testFactory() {
		DefaultServiceDiscoveryFactory factory = new DefaultServiceDiscoveryFactory();
		ServiceDiscovery instance = factory.getInstance();
		Assert.assertNotNull(instance);
		Assert.assertEquals(CdiServiceDiscovery.class, instance.getClass());

		List<?> repositories = instance.getInstancesByType(Repository.class);
		Assert.assertEquals(1, repositories.size());
		Assert.assertTrue(repositories.get(0) instanceof ProjectRepository);

		repositories = instance.getInstancesByAnnotation(JsonApiExposed.class);
		Assert.assertEquals(1, repositories.size());
		Assert.assertTrue(repositories.get(0) instanceof TaskRepository);
	}

	@Test
	public void testExceptionMapper() {
		CrnkBoot boot = new CrnkBoot();
		boot.boot();

		Optional<ExceptionMapper> mapper = boot.getExceptionMapperRegistry().findMapperFor(IllegalStateException.class);
		Assert.assertTrue(mapper.isPresent());
		Assert.assertTrue(mapper.get() instanceof CdiTestExceptionMapper);
	}
}
