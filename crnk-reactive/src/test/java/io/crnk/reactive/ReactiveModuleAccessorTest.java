package io.crnk.reactive;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ReactiveModuleAccessorTest {

	@Test
	public void check() {
		ReactiveModule module = new ReactiveModule();
		Assert.assertEquals("reactive", module.getModuleName());

		Assert.assertEquals(Schedulers.elastic(), module.getWorkerScheduler());

		Scheduler scheduler = Mockito.mock(Scheduler.class);
		module.setWorkerScheduler(scheduler);
		Assert.assertSame(scheduler, module.getWorkerScheduler());
	}
}
