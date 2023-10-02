package io.crnk.internal.boot.cdi;

import io.crnk.cdi.internal.CdiTransactionRunner;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.module.discovery.DefaultServiceDiscoveryFactory;
import io.crnk.core.module.discovery.ServiceDiscovery;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.TransactionalException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@RunWith(CdiTestRunner.class)
@ApplicationScoped
public class CdiTransactionRunnerTest {


	private TransactionRunner runner;

	@Before
	public void setup() {
		DefaultServiceDiscoveryFactory factory = new DefaultServiceDiscoveryFactory();
		ServiceDiscovery instance = factory.getInstance();
		List<TransactionRunner> runners = instance.getInstancesByType(TransactionRunner.class);
		Assert.assertEquals(1, runners.size());
		runner = runners.get(0);
	}

	@Test
	public void test() throws Exception {
		Callable callable = Mockito.mock(Callable.class);
		runner.doInTransaction(callable);

		Mockito.verify(callable, Mockito.times(1)).call();
	}


	@Test
	public void testHasPublicNoArgConstructor() {
		Assert.assertNotNull(new CdiTransactionRunner());
	}


	@Test
	public void testTransactionalRuntimeExceptionToBeUnwrapped() throws Exception {
		Callable callable = Mockito.mock(Callable.class);
		Mockito.when(callable.call()).thenThrow(new TransactionalException("a", new IllegalStateException("b")));
		try {
			runner.doInTransaction(callable);
			Assert.fail();
		} catch (IllegalStateException e) {
			Assert.assertEquals("b", e.getMessage());
		}
		Mockito.verify(callable, Mockito.times(1)).call();
	}


	@Test
	public void testTransactionalExceptionNotToBeUnwrapped() throws Exception {
		Callable callable = Mockito.mock(Callable.class);
		Mockito.when(callable.call()).thenThrow(new TransactionalException("a", new IOException("b")));
		try {
			runner.doInTransaction(callable);
			Assert.fail();
		} catch (TransactionalException e) {
			Assert.assertEquals("a", e.getMessage());
		}
		Mockito.verify(callable, Mockito.times(1)).call();
	}

	@Test
	public void testRuntimeExceptionToPassThrough() throws Exception {
		Callable callable = Mockito.mock(Callable.class);
		Mockito.when(callable.call()).thenThrow(new IllegalStateException("b"));
		try {
			runner.doInTransaction(callable);
			Assert.fail();
		} catch (IllegalStateException e) {
			Assert.assertEquals("b", e.getMessage());
		}
		Mockito.verify(callable, Mockito.times(1)).call();
	}

	@Test
	public void testExceptionToBeWrapped() throws Exception {
		Callable callable = Mockito.mock(Callable.class);
		Mockito.when(callable.call()).thenThrow(new Exception("b"));
		try {
			runner.doInTransaction(callable);
			Assert.fail();
		} catch (IllegalStateException e) {
			Assert.assertEquals("b", e.getCause().getMessage());
		}
		Mockito.verify(callable, Mockito.times(1)).call();
	}
}
