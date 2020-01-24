package io.crn.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.repository.Repository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.guice.GuiceServiceDiscovery;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.TestExceptionMapper;
import io.crnk.test.mock.repository.ProjectRepository;
import io.crnk.test.mock.repository.TaskRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class GuiceServiceDiscoveryTest {

	private GuiceServiceDiscovery discovery;

	public class TestModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(ProjectRepository.class);
			bind(TaskRepository.class);
			bind(TestExceptionMapper.class);
		}
	}

	@Before
	public void setup() {
		Injector injector = Guice.createInjector(new TestModule());
		discovery = new GuiceServiceDiscovery(injector);
	}


	@Test
	public void repositoryDiscovery() {
		List<?> repositories = discovery.getInstancesByType(Repository.class);
		Assert.assertEquals(2, repositories.size());
		Assert.assertTrue(repositories.get(0) instanceof ProjectRepository);

		repositories = discovery.getInstancesByAnnotation(JsonApiExposed.class);
		Assert.assertEquals(1, repositories.size());
		Assert.assertTrue(repositories.get(0) instanceof TaskRepository);
	}

	@Test
	public void exceptionMapperDiscovery() {
		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(discovery);
		boot.boot();

		Optional<ExceptionMapper> mapper = boot.getExceptionMapperRegistry().findMapperFor(TestException.class);
		Assert.assertTrue(mapper.isPresent());
		Assert.assertTrue(mapper.get() instanceof TestExceptionMapper);
	}
}
